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
    private final RazorpayClient razorpayClient;
    private final CartService cartService;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        User user = userRepository.getByUserId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));
        Order order = convertToEntity(request, user);

        // Initialize payment details based on payment method
        PaymentDetails paymentDetails = new PaymentDetails();

        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            // Cash on delivery - payment is pending until delivery
            paymentDetails.setPaymentStatus(PaymentDetails.PaymentStatus.PENDING);
            order.setStatus(OrderStatus.CONFIRMED); // Order is confirmed but payment pending
            order.setDeliveryCharges(request.getDeliveryCharges());
        } else {
            // Online payment - payment and order both pending until verification
            paymentDetails.setPaymentStatus(PaymentDetails.PaymentStatus.PENDING);
            order.setStatus(OrderStatus.PENDING); // Order pending until payment verification
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
        Order existingOrder = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Validate status transition before updating
        if (request.getStatus() != null) {
            validateStatusTransition(existingOrder.getStatus(), request.getStatus(), existingOrder.getPaymentDetails());
            existingOrder.setStatus(request.getStatus());
        }

        existingOrder.setTotalAmount(request.getTotalAmount());

        // Update shipping address
        if (request.getShippingAddress() != null) {
            Address existingAddress = getAddress(request, existingOrder);
            addressRepository.save(existingAddress);
        }

        // Replace order items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
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

        Order savedOrder = orderRepository.save(existingOrder);
        return convertToResponse(savedOrder);
    }

    @Override
    public void deleteOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not Found"));

        OrderStatus orderStatus = order.getStatus();
        PaymentDetails.PaymentStatus paymentStatus = order.getPaymentDetails() != null ?
                order.getPaymentDetails().getPaymentStatus() : PaymentDetails.PaymentStatus.PENDING;

        if (orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.CONFIRMED || orderStatus == OrderStatus.PROCESSING) {
            orderRepository.delete(order);
        } else {
            throw new RuntimeException("Order cannot be deleted - either payment is completed or order is in non-deletable status");
        }
    }

    @Override
    public OrderResponse verifyPayment(PaymentVerificationRequest request) {
        try {
            Order existingOrder = orderRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order Not Found"));

            // Check if order is in correct state for payment verification
            if (existingOrder.getStatus() != OrderStatus.PENDING) {
                throw new RuntimeException("Order is not in pending state for payment verification");
            }

            // Verify Razorpay signature
            if (!verifyRazorpaySignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature())) {
                throw new RuntimeException("Payment Verification Failed");
            }

            // Capture the payment
            Payment payment = razorpayClient.payments.fetch(request.getRazorpayPaymentId());

            if ("authorized".equals(payment.get("status"))) {
                JSONObject captureRequest = new JSONObject();
                captureRequest.put("amount", Integer.valueOf(payment.get("amount").toString()));
                captureRequest.put("currency", payment.get("currency").toString());

                Payment capturedPayment = razorpayClient.payments.capture(request.getRazorpayPaymentId(), captureRequest);

                if (!"captured".equals(capturedPayment.get("status"))) {
                    throw new RuntimeException("Payment capture failed");
                }
            }

            // Update payment details
            PaymentDetails paymentDetails = existingOrder.getPaymentDetails();
            if (paymentDetails == null) {
                paymentDetails = new PaymentDetails();
            }
            paymentDetails.setRazorpayOrderId(request.getRazorpayOrderId());
            paymentDetails.setRazorpayPaymentId(request.getRazorpayPaymentId());
            paymentDetails.setRazorpaySignature(request.getRazorpaySignature());
            paymentDetails.setPaymentStatus(PaymentDetails.PaymentStatus.COMPLETED);

            existingOrder.setPaymentDetails(paymentDetails);

            // Update order status to CONFIRMED only after successful payment
            existingOrder.setStatus(OrderStatus.CONFIRMED);

            existingOrder = orderRepository.save(existingOrder);
            return convertToResponse(existingOrder);

        } catch (Exception e) {
            e.printStackTrace();

            // Handle payment failure
            Order existingOrder = orderRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order Not Found"));

            PaymentDetails paymentDetails = existingOrder.getPaymentDetails();
            if (paymentDetails == null) {
                paymentDetails = new PaymentDetails();
            }
            paymentDetails.setPaymentStatus(PaymentDetails.PaymentStatus.FAILED);
            existingOrder.setPaymentDetails(paymentDetails);

            // Order remains PENDING, don't automatically cancel
            // Let admin or system decide what to do with failed payment orders

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
    public OrderResponse updateStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus, order.getPaymentDetails());

        order.setStatus(newStatus);

        // Handle specific status updates
        switch (newStatus) {
            case SHIPPED -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED -> {
                order.setDeliveredAt(LocalDateTime.now());
                // For COD orders, mark payment as completed when delivered
                if (order.getPaymentMethod() == PaymentMethod.CASH &&
                        order.getPaymentDetails().getPaymentStatus() == PaymentDetails.PaymentStatus.PENDING) {
                    order.getPaymentDetails().setPaymentStatus(PaymentDetails.PaymentStatus.COMPLETED);
                }
            }
            case CANCELLED -> {
                // If payment was completed, it might need refund processing
                if (order.getPaymentDetails().getPaymentStatus() == PaymentDetails.PaymentStatus.COMPLETED) {
                    // Don't automatically change payment status to FAILED
                    // This should be handled by refund process
                }
            }
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToResponse(updatedOrder);
    }

    @Override
    public TrackOrderResponse trackOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        String step = switch (order.getStatus()) {
            case PENDING -> "Pending Payment Verification";
            case CONFIRMED -> "Confirmed - Processing Soon";
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

    /**
     * Validates if the status transition is allowed based on current order status and payment status
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus, PaymentDetails paymentDetails) {
        PaymentDetails.PaymentStatus paymentStatus = paymentDetails != null ?
                paymentDetails.getPaymentStatus() : PaymentDetails.PaymentStatus.PENDING;

        switch (currentStatus) {
            case PENDING -> {
                // From PENDING, can only go to CONFIRMED (after payment) or CANCELLED
                if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from PENDING to " + newStatus);
                }
            }
            case CONFIRMED -> {
                // From CONFIRMED, can go to PROCESSING or CANCELLED
                if (newStatus != OrderStatus.PROCESSING && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from CONFIRMED to " + newStatus);
                }
            }
            case PROCESSING -> {
                // From PROCESSING, can go to SHIPPED or CANCELLED
                if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from PROCESSING to " + newStatus);
                }
            }
            case SHIPPED -> {
                // From SHIPPED, can go to OUT_FOR_DELIVERY or DELIVERED
                if (newStatus != OrderStatus.OUT_FOR_DELIVERY && newStatus != OrderStatus.DELIVERED) {
                    throw new IllegalStateException("Invalid status transition from SHIPPED to " + newStatus);
                }
            }
            case OUT_FOR_DELIVERY -> {
                // From OUT_FOR_DELIVERY, can only go to DELIVERED
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new IllegalStateException("Invalid status transition from OUT_FOR_DELIVERY to " + newStatus);
                }
            }
            case DELIVERED -> {
                // From DELIVERED, can go to RETURNED (via return process)
                if (newStatus != OrderStatus.RETURNED) {
                    throw new IllegalStateException("Invalid status transition from DELIVERED to " + newStatus);
                }
            }
            case CANCELLED, RETURNED, REFUNDED -> {
                // These are terminal states
                throw new IllegalStateException("Cannot change status from terminal state " + currentStatus);
            }
        }
    }

    private boolean verifyRazorpaySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            String data = razorpayOrderId + "|" + razorpayPaymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes());

            StringBuilder actualSignature = new StringBuilder();
            for (byte b : hash) {
                actualSignature.append(String.format("%02x", b));
            }

            return actualSignature.toString().equals(razorpaySignature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    /**
     * Converts OrderRequest and User into Order entity.
     */
    private Order convertToEntity(OrderRequest request, User user) {
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

        Order order = Order.builder()
                .user(user)
                .shippingAddress(address)
                .totalAmount(request.getTotalAmount())
                .orderDate(LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING) // Always start with PENDING
                .build();

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
                .deliveredAt(order.getDeliveredAt())
                .shippedAt(order.getShippedAt())
                .estimatedDelivery(order.getEstimatedDelivery())
                .build();
    }
}