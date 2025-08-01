package com.quodex._miles.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private String cartId;
    private List<CartItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime expectedDate;
    private String userId;
    private Double subTotal;
    private Double totalDiscount;
    private Double totalTax;
    private Double grandTotal;
}
