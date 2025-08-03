package com.quodex._miles.service.Impl;

import com.quodex._miles.constant.OrderStatus;
import com.quodex._miles.constant.PaymentMethod;
import com.quodex._miles.entity.Address;
import com.quodex._miles.entity.Order;
import com.quodex._miles.entity.OrderItem;
import com.quodex._miles.entity.User;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.*;
import com.quodex._miles.repository.AddressRepository;
import com.quodex._miles.repository.OrderRepository;
import com.quodex._miles.repository.UserRepository;
import com.quodex._miles.service.CartService;
import com.quodex._miles.service.OrderService;
import com.razorpay.RazorpayClient;
import com.razorpay.Payment;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final RazorpayClient razorpayClient; // Add this dependency
    private final CartService cartService;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        User user = userRepository.getByUserId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));
        Order order = convertToEntity(request, user);

        // Initialize payment details with PENDING status for online payments
        PaymentDetails paymentDetails = new PaymentDetails();
        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            paymentDetails.setPaymentStatus(PaymentDetails.PaymentStatus.COMPLETED);
            order.setDeliveryCharges(request.getDeliveryCharges());
        } else {
            paymentDetails.setPaymentStatus(PaymentDetails.PaymentStatus.PENDING);
        }
        order.setPaymentDetails(paymentDetails);
        order.setEstimatedDelivery(LocalDateTime.now().plusDays(10));
        // Save shipping address first
        addressRepository.save(order.getShippingAddress());
        Order savedOrder = orderRepository.save(order);
        cartService.deleteCartByUser(request.getUserId());
        return convertToResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> getOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderByOrderId(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order Not Found"));
        return convertToResponse(order);
    }

    @Override
    public OrderResponse updateOrder(String orderId, OrderRequest request) {
        // Fetch existing order
        Order existingOrder = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Update status if provided
        if (request.getStatus() != null) {
            existingOrder.setStatus(request.getStatus());
        }

        // Always update totalAmount since it's a primitive
        existingOrder.setTotalAmount(request.getTotalAmount());

        // Update shipping address
        if (request.getShippingAddress() != null) {
            Address existingAddress = getAddress(request, existingOrder);
            addressRepository.save(existingAddress);
        }

        // Replace order items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Clear old items (only if orphanRemoval = true in @OneToMany)
            existingOrder.getItems().clear();

            List<OrderItem> updatedItems = request.getItems().stream()
                    .map(itemReq -> OrderItem.builder()
                            .productId(itemReq.getProductId())
                            .productName(itemReq.getProductName())
                            .color(itemReq.getColor())
                            .size(itemReq.getSize())
                            .image(itemReq.getImage())
                            .quantity(itemReq.getQuantity())
                            .price(itemReq.getPrice())
                            .total(itemReq.getTotal())
                            .order(existingOrder)
                            .build()
                    ).toList();

            existingOrder.getItems().addAll(updatedItems);
        }

        // Save updated order
        Order savedOrder = orderRepository.save(existingOrder);

        return convertToResponse(savedOrder);
    }

    private static Address getAddress(OrderRequest request, Order existingOrder) {
        Address existingAddress = existingOrder.getShippingAddress();
        AddressRequest newAddressData = request.getShippingAddress();

        existingAddress.setFullName(newAddressData.getFullName());
        existingAddress.setPhone(newAddressData.getPhone());
        existingAddress.setEmail(newAddressData.getEmail());
        existingAddress.setStreet(newAddressData.getStreet());
        existingAddress.setCity(newAddressData.getCity());
        existingAddress.setState(newAddressData.getState());
        existingAddress.setPostalCode(newAddressData.getPostalCode());
        existingAddress.setCountry(newAddressData.getCountry());
        return existingAddress;
    }

    private Address convertToAddressEntity(AddressRequest addressRequest) {
        return Address.builder()
                .fullName(addressRequest.getFullName())
                .phone(addressRequest.getPhone())
                .email(addressRequest.getEmail())
                .street(addressRequest.getStreet())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .postalCode(addressRequest.getPostalCode())
                .country(addressRequest.getCountry())
                .build();
    }

    @Override
    public void deleteOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not Found"));
        OrderStatus orderStatus = order.getStatus();
        if (orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.PROCESSING || orderStatus == OrderStatus.CONFIRMED){
            orderRepository.delete(order);
        } else {
           throw new RuntimeException("Order Deletion Not Allowed");
        }

    }

    @Override
    public OrderResponse verifyPayment(PaymentVerificationRequest request) {
        try {
            // 1. Find the order in the database by its order ID
            Order existingOrder = orderRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order Not Found"));

            // 2. Verify Razorpay signature to ensure the payment is valid and secure
            if (!verifyRazorpaySignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature())) {
                throw new RuntimeException("Payment Verification Failed");
            }

            // 3. Capture the payment
            Payment payment = razorpayClient.payments.fetch(request.getRazorpayPaymentId());

            // Check if payment needs to be captured (status should be "authorized")
            if ("authorized".equals(payment.get("status"))) {
                // Capture the full amount using RazorpayClient
                JSONObject captureRequest = new JSONObject();
                captureRequest.put("amount", Integer.valueOf(payment.get("amount").toString())); // Amount in paise
                captureRequest.put("currency", payment.get("currency").toString());

                // Use razorpayClient to capture the payment
                Payment capturedPayment = razorpayClient.payments.capture(request.getRazorpayPaymentId(), captureRequest);

                // Check if capture was successful
                if (!"captured".equals(capturedPayment.get("status"))) {
                    throw new RuntimeException("Payment capture failed");
                }
            }

            // 4. Update the payment details in the order
            PaymentDetails paymentDetails = existingOrder.getPaymentDetails();
            if (paymentDetails == null) {
                paymentDetails = new PaymentDetails();
            }
            paymentDetails.setRazorpayOrderId(request.getRazorpayOrderId());
            paymentDetails.setRazorpayPaymentId(request.getRazorpayPaymentId());
            paymentDetails.setRazorpaySignature(request.getRazorpaySignature());
            paymentDetails.setPaymentStatus(PaymentDetails.PaymentStatus.COMPLETED); // Mark payment as completed

            existingOrder.setPaymentDetails(paymentDetails);

            // Also update order status to CONFIRMED if payment is successful
            existingOrder.setStatus(OrderStatus.CONFIRMED);

            // 5. Save updated order to the database
            existingOrder = orderRepository.save(existingOrder);

            // 6. Return the updated order response
            return convertToResponse(existingOrder);

        } catch (Exception e) {
            // Log the error and mark payment as failed
            e.printStackTrace();

            // Update order with failed payment status
            Order existingOrder = orderRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order Not Found"));

            PaymentDetails paymentDetails = existingOrder.getPaymentDetails();
            if (paymentDetails == null) {
                paymentDetails = new PaymentDetails();
            }
            paymentDetails.setPaymentStatus(PaymentDetails.PaymentStatus.FAILED);
            existingOrder.setPaymentDetails(paymentDetails);
            existingOrder.setStatus(OrderStatus.CANCELLED);

            orderRepository.save(existingOrder);

            throw new RuntimeException("Payment verification and capture failed: " + e.getMessage());
        }
    }

    @Override
    public List<OrderResponse> getOrderByUser(String userId) {
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return orderRepository.findByUser_UserId(userId)
                .stream().map(this::convertToResponse).toList();

    }

    @Override
    public OrderResponse updateStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setStatus(status);
        if (status == OrderStatus.SHIPPED){
        order.setShippedAt(LocalDateTime.now());
        } else if (status == OrderStatus.DELIVERED){
            order.setDeliveredAt(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);

        return convertToResponse(updatedOrder); // assuming you have a method to convert Orders to OrderResponse
    }

    @Override
    public TrackOrderResponse trackOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        String step = switch (order.getStatus()) {
            case PENDING -> "Pending";
            case CONFIRMED -> "Confirmed";
            case PROCESSING -> "Processing your order";
            case SHIPPED -> "Shipped";
            case OUT_FOR_DELIVERY -> "Out for delivery";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
            case RETURNED -> "Returned";
            case REFUNDED -> "Refunded";
        };

        return TrackOrderResponse.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .placedAt(order.getOrderDate())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .estimatedDelivery(order.getEstimatedDelivery())
                .currentStep(step)
                .build();
    }

    private boolean verifyRazorpaySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            // 1. Concatenate orderId and paymentId using a pipe (|)
            String data = razorpayOrderId + "|" + razorpayPaymentId;

            // 2. Create a Mac instance using HMAC SHA256
            Mac mac = Mac.getInstance("HmacSHA256");

            // 3. Initialize the Mac with your Razorpay secret key
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    razorpayKeySecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);

            // 4. Generate the hash from the data
            byte[] hash = mac.doFinal(data.getBytes());

            // 5. Convert hash bytes to hexadecimal format
            StringBuilder actualSignature = new StringBuilder();
            for (byte b : hash) {
                actualSignature.append(String.format("%02x", b));
            }

            // 6. Compare the actualSignature with Razorpay's signature (timing-safe)
            return actualSignature.toString().equals(razorpaySignature);
        } catch (Exception e) {
            // Log the error if needed and return false
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Converts OrderRequest and User into Order entity.
     */
    private Order convertToEntity(OrderRequest request, User user) {
        // Convert address
        Address address = Address.builder()
                .fullName(request.getShippingAddress().getFullName())
                .phone(request.getShippingAddress().getPhone())
                .email(request.getShippingAddress().getEmail())
                .street(request.getShippingAddress().getStreet())
                .city(request.getShippingAddress().getCity())
                .state(request.getShippingAddress().getState())
                .postalCode(request.getShippingAddress().getPostalCode())
                .country(request.getShippingAddress().getCountry())
                .build();

        // Create order
        Order order = Order.builder()
                .user(user)
                .shippingAddress(address)
                .totalAmount(request.getTotalAmount())
                .orderDate(LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .status(request.getStatus() != null ? request.getStatus() : OrderStatus.PENDING)
                .build();

        // Map order items and set back reference
        List<OrderItem> orderItems = request.getItems().stream().map(itemReq -> OrderItem.builder()
                .productId(itemReq.getProductId())
                .productName(itemReq.getProductName())
                .color(itemReq.getColor())
                .size(itemReq.getSize())
                .image(itemReq.getImage())
                .quantity(itemReq.getQuantity())
                .price(itemReq.getPrice())
                .order(order)
                .total(itemReq.getTotal())
                .build()).collect(Collectors.toList());

        order.setItems(orderItems);

        return order;
    }

    private OrderResponse convertToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
            OrderItemResponse response = new OrderItemResponse();
            response.setOrderItemId(item.getOrderItemId());
            response.setOrderId(order.getOrderId());
            response.setProductId(item.getProductId());
            response.setProductName(item.getProductName());
            response.setColor(item.getColor());
            response.setSize(item.getSize());
            response.setQuantity(item.getQuantity());
            response.setPrice(item.getPrice());
            response.setImage(item.getImage());
            response.setTotal(item.getTotal());
            return response;
        }).toList();

        AddressResponse addressResponse = AddressResponse.builder()
                .fullName(order.getShippingAddress().getFullName())
                .phone(order.getShippingAddress().getPhone())
                .email(order.getShippingAddress().getEmail())
                .street(order.getShippingAddress().getStreet())
                .city(order.getShippingAddress().getCity())
                .state(order.getShippingAddress().getState())
                .postalCode(order.getShippingAddress().getPostalCode())
                .country(order.getShippingAddress().getCountry())
                .build();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .items(itemResponses)
                .shippingAddress(addressResponse)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryCharges(order.getDeliveryCharges())
                .orderDate(order.getOrderDate())
                .paymentDetails(order.getPaymentDetails())
                .paymentMethod(order.getPaymentMethod())
                .build();
    }
}