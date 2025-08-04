package com.quodex._miles.controller;

import com.quodex._miles.constant.ReturnStatus;
import com.quodex._miles.io.ReturnProcessRequest;
import com.quodex._miles.io.ReturnRequest;
import com.quodex._miles.io.ReturnResponse;
import com.quodex._miles.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/return")
@RequiredArgsConstructor
public class ReturnRequestController {
    private final ReturnRequestService returnService;

    @PostMapping("/request")
    public ResponseEntity<ReturnResponse> addReturnRequest(@RequestBody ReturnRequest request){
        ReturnResponse response = returnService.addReturnRequest(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process/{requestId}")
    public ResponseEntity<ReturnResponse> processReturns(@PathVariable String requestId, @RequestBody ReturnProcessRequest request){
        ReturnResponse response = returnService.processReturnRequest(requestId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ReturnResponse> getReturnItemById(@PathVariable String requestId){
        ReturnResponse response = returnService.getReturnByReturnId(requestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<ReturnResponse> getReturnByOrder(@PathVariable String orderId) throws IllegalAccessException {
        ReturnResponse response = returnService.getReturnByOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReturnResponse>> getReturnsByUser(@PathVariable String userId){
        List<ReturnResponse> responses = returnService.getReturnsByUser(userId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<String> deleteReturns(@PathVariable String requestId){
        returnService.deleteReturnRequest(requestId);
        return ResponseEntity.ok("Return Item Deleted");
    }

}
