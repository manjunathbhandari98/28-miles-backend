package com.quodex._miles.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {
    private Integer amount;      // Amount in rupees (will be converted to paise)
    private String currency;     // e.g., "INR"
    private String customerId;   // Optional: for tracking
    private String orderId;      // Your internal order ID for reference
}