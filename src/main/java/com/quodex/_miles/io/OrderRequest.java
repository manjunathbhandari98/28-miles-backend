package com.quodex._miles.io;

import com.quodex._miles.constant.OrderStatus;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private String userId;
    private List<OrderItemRequest> items;
    private AddressRequest shippingAddress;
    private double totalAmount;
    private OrderStatus status; // e.g., PENDING, SHIPPED, DELIVERED, CANCELLED
    private String paymentMode;
    private Double deliveryCharges;

}
