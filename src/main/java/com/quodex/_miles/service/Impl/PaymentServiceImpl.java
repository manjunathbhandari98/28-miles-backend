package com.quodex._miles.service.Impl;

import com.quodex._miles.io.PaymentRequest;
import com.quodex._miles.io.PaymentResponse;
import com.quodex._miles.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final RazorpayClient razorpayClient;

    @Override
    public PaymentResponse createPaymentOrder(PaymentRequest request) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", request.getAmount() * 100); // Amount in paise
        orderRequest.put("currency", request.getCurrency());
        orderRequest.put("receipt", "order_rcpid_" + System.currentTimeMillis());

        // Set payment_capture to 0 for manual capture after verification
        // This allows you to capture the payment later after verification
        orderRequest.put("payment_capture", 0);

        // Optional: Add notes for better tracking
        JSONObject notes = new JSONObject();
        if (request.getCustomerId() != null) {
            notes.put("customer_id", request.getCustomerId());
        }
        notes.put("order_type", "ecommerce");
        orderRequest.put("notes", notes);

        Order order = razorpayClient.orders.create(orderRequest);

        return convertToResponse(order);
    }

    private PaymentResponse convertToResponse(Order order) {
        return PaymentResponse.builder()
                .id(order.get("id"))                   // Razorpay order ID
                .entity(order.get("entity"))           // Entity type (usually "order")
                .amount(order.get("amount"))           // Amount in paise
                .currency(order.get("currency"))       // Currency code (e.g., "INR")
                .status(order.get("status"))           // Order status (e.g., "created")
                .createdAt(LocalDateTime.now())        // Timestamp of order creation
                .receipt(order.get("receipt"))         // Receipt ID
                .build();
    }
}