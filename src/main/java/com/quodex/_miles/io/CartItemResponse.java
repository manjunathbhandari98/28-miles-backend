package com.quodex._miles.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponse {
    private String cartItemId;
    private String cartId;
    private String productId;
    private String productName;
    private String size;
    private String color;
    private int quantity;
    private double price;
    private String image;
}
