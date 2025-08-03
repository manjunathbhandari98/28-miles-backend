package com.quodex._miles.controller;

import com.quodex._miles.constant.OrderStatus;
import com.quodex._miles.io.OrderRequest;
import com.quodex._miles.io.OrderResponse;
import com.quodex._miles.io.TrackOrderResponse;
import com.quodex._miles.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request){
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(){
        List<OrderResponse> response = orderService.getOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrderByUser(@PathVariable String userId){
        List<OrderResponse> response = orderService.getOrderByUser(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId){
        OrderResponse response = orderService.getOrderByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/status/{orderId}")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable String orderId, @RequestBody OrderStatus status){
        OrderResponse response = orderService.updateStatus(orderId, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/track/{orderId}")
    public ResponseEntity<TrackOrderResponse> trackOrder(@PathVariable String orderId) {
        TrackOrderResponse response = orderService.trackOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String orderId,
                                                     @RequestBody OrderRequest request){
        OrderResponse orderResponse = orderService.updateOrder(orderId,request);
        return ResponseEntity.ok(orderResponse);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable String orderId){
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok("Order Deleted Successfully");
    }
}
