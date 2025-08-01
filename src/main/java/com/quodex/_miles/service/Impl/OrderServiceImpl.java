package com.quodex._miles.service.Impl;

import com.quodex._miles.constant.OrderStatus;
import com.quodex._miles.entity.Address;
import com.quodex._miles.entity.Order;
import com.quodex._miles.entity.OrderItem;
import com.quodex._miles.entity.User;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.*;
import com.quodex._miles.repository.AddressRepository;
import com.quodex._miles.repository.OrderRepository;
import com.quodex._miles.repository.UserRepository;
import com.quodex._miles.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        User user = userRepository.getByUserId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));
        Order order = convertToEntity(request, user);

        // Save shipping address first
        addressRepository.save(order.getShippingAddress());
        Order savedOrder = orderRepository.save(order);
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
        orderRepository.delete(order);
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
                .paymentMode(order.getPaymentMode())
                .build();
    }
}


