package com.quodex._miles.service;

import com.quodex._miles.constant.OrderStatus;
import com.quodex._miles.io.OrderRequest;
import com.quodex._miles.io.OrderResponse;
import com.quodex._miles.io.PaymentVerificationRequest;
import com.quodex._miles.io.TrackOrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request);
    List<OrderResponse> getOrders();
    OrderResponse getOrderByOrderId(String orderId);
    OrderResponse updateOrder(String OrderId, OrderRequest request);
    void deleteOrder(String orderId);
    OrderResponse verifyPayment(PaymentVerificationRequest request);

    List<OrderResponse> getOrderByUser(String userId);

    OrderResponse updateStatus(String orderId, OrderStatus status);

    TrackOrderResponse trackOrder(String orderId);

}
