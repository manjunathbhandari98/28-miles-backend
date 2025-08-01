package com.quodex._miles.io;

import com.quodex._miles.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemRequest {
    private String orderId;

    private String productId;
    private String productName;
    private String size;
    private String color;
    private int quantity;
    private double price;
    private String image;
    private Double total;
}
