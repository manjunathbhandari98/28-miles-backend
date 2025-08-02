package com.quodex._miles.controller;

import com.quodex._miles.io.OrderResponse;
import com.quodex._miles.io.PaymentRequest;
import com.quodex._miles.io.PaymentResponse;
import com.quodex._miles.io.PaymentVerificationRequest;
import com.quodex._miles.service.OrderService;
import com.quodex._miles.service.PaymentService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/payment")
@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/create-order")
    public PaymentResponse createRazorpayOrder(@RequestBody PaymentRequest request) throws RazorpayException {
        return paymentService.createPaymentOrder(request);
    }

    @PostMapping("/verify")
    public OrderResponse verifyPayment(@RequestBody PaymentVerificationRequest request){
        return orderService.verifyPayment(request);
    }

}
