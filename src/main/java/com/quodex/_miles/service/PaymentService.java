package com.quodex._miles.service;

import com.quodex._miles.io.PaymentRequest;
import com.quodex._miles.io.PaymentResponse;
import com.razorpay.RazorpayException;
import netscape.javascript.JSObject;
import org.json.JSONObject;

public interface PaymentService {
    PaymentResponse createPaymentOrder(PaymentRequest request) throws RazorpayException;
}
