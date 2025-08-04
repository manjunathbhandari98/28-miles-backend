package com.quodex._miles.io;

import com.quodex._miles.constant.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

import com.quodex._miles.constant.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String orderId;
    private String userId;
    private List<OrderItemResponse> items;
    private AddressResponse shippingAddress;
    private double totalAmount;
    private OrderStatus status; // e.g., PENDING, SHIPPED, DELIVERED, CANCELLED
    private Double deliveryCharges;
    private PaymentMethod paymentMethod;
    private LocalDateTime orderDate;
    private PaymentDetails paymentDetails;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime estimatedDelivery;

}
