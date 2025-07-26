package com.quodex._miles.service;

import com.quodex._miles.io.OrderRequest;
import com.quodex._miles.io.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request);
    List<OrderResponse> getOrders();
    OrderResponse getOrderByOrderId(String orderId);
    OrderResponse updateOrder(String OrderId, OrderRequest request);
    void deleteOrder(String orderId);
}
