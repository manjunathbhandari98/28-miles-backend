package com.quodex._miles.service.Impl;

import com.quodex._miles.entity.Order;
import com.quodex._miles.entity.OrderItem;
import com.quodex._miles.entity.Return;
import com.quodex._miles.entity.User;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.ReturnProcessRequest;
import com.quodex._miles.io.ReturnRequest;
import com.quodex._miles.io.ReturnResponse;
import com.quodex._miles.repository.OrderRepository;
import com.quodex._miles.repository.ReturnRequestRepository;
import com.quodex._miles.repository.UserRepository;
import com.quodex._miles.service.ReturnRequestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // 1. From User: Create Return Request
    @Override
    public ReturnResponse addReturnRequest(ReturnRequest dto) {
        Return entity = convertToEntity(dto);
        Return saved = returnRepository.save(entity);
        return convertToResponse(saved);
    }

    // 2. From Admin: Process Return Request
    @Override
    public ReturnResponse processReturnRequest(String requestId, ReturnProcessRequest dto) {
        Return entity = returnRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Return Request not found"));

        entity.setStatus(dto.getStatus());
        entity.setProcessedAt(LocalDateTime.now());
        entity.setComments(dto.getComments());

        Return updated = returnRepository.save(entity);
        return convertToResponse(updated);
    }

    @Override
    public List<ReturnResponse> getReturnsByUser(String userId) {
        return returnRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public ReturnResponse getReturnByReturnId(String returnId) {
        Return returnEntity = returnRepository.findByRequestId(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Item Not Found"));
        return convertToResponse(returnEntity);
    }

    @Override
    public void deleteReturnRequest(String returnId) {
        Return returnEntity = returnRepository.findByRequestId(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Item Not Found"));
        returnRepository.delete(returnEntity);
    }

    // Conversion for User's ReturnRequest DTO → Entity
    private Return convertToEntity(ReturnRequest dto) {
        User user = userRepository.getByUserId(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findByOrderId(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getOrderItemId().equals(dto.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("OrderItem not found"));

        return Return.builder()
                .user(user)
                .order(order)
                .orderItem(orderItem)
                .reason(dto.getReason())
                .build();
    }

    // Entity → ReturnResponse
    private ReturnResponse convertToResponse(Return entity) {
        return ReturnResponse.builder()
                .returnId(entity.getRequestId())
                .userId(entity.getUser().getUserId())
                .orderId(entity.getOrder().getOrderId())
                .orderItemId(entity.getOrderItem().getOrderItemId())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .processedAt(entity.getProcessedAt())
                .comments(entity.getComments())
                .build();
    }
}
