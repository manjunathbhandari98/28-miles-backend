package com.quodex._miles.service.Impl;

import com.quodex._miles.constant.OrderStatus;
import com.quodex._miles.constant.PaymentMethod;
import com.quodex._miles.constant.ReturnStatus;
import com.quodex._miles.entity.Order;
import com.quodex._miles.entity.OrderItem;
import com.quodex._miles.entity.Return;
import com.quodex._miles.entity.User;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.PaymentDetails;
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
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    public ReturnResponse addReturnRequest(ReturnRequest request) {
        Order order = orderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order Not Found"));

        // Validate order is delivered and payment is completed
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Return request can only be created for delivered orders");
        }

        // Check if payment is completed (important for refund processing)
        if (order.getPaymentDetails() == null ||
                order.getPaymentDetails().getPaymentStatus() != PaymentDetails.PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Return request can only be created for orders with completed payment");
        }

        LocalDateTime deliveredAt = order.getDeliveredAt();
        if (deliveredAt == null) {
            throw new IllegalStateException("Order delivery date not found");
        }

        LocalDateTime returnDeadline = deliveredAt.plusDays(7);

        if (LocalDateTime.now().isAfter(returnDeadline)) {
            throw new IllegalStateException("Return window has expired (Only 7 days from delivery allowed)");
        }

        Return entity = convertToEntity(request);
        if(order.getPaymentMethod() != PaymentMethod.CASH){
        entity.setStatus(ReturnStatus.APPROVED);

        } else {
            entity.setStatus(ReturnStatus.PENDING);
        }

        Return saved = returnRepository.save(entity);
        return convertToResponse(saved);
    }

    @Override
    public ReturnResponse processReturnRequest(String requestId, ReturnProcessRequest dto) {
        Return entity = returnRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Return Request not found"));

        // Validate status transition
        validateReturnStatusTransition(entity.getStatus(), dto.getStatus());

        entity.setStatus(dto.getStatus());
        entity.setProcessedAt(LocalDateTime.now());
        entity.setComments(dto.getComments());

        // If return is completed, update the order status
        if (dto.getStatus() == ReturnStatus.COMPLETED) {
            Order order = entity.getOrder();
            order.setStatus(OrderStatus.RETURNED);
            orderRepository.save(order);
        }

        Return updated = returnRepository.save(entity);
        return convertToResponse(updated);
    }

    @Override
    public List<ReturnResponse> getReturnsByUser(String userId) {
        // Fixed: This should filter by user, not return all returns
        return returnRepository.findByUser_UserId(userId)
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

        // Only allow deletion of pending return requests
        if (returnEntity.getStatus() != ReturnStatus.PENDING) {
            throw new IllegalStateException("Cannot delete return request in " + returnEntity.getStatus() + " status");
        }

        returnRepository.delete(returnEntity);
    }

    @Override
    public ReturnResponse getReturnByOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order Not Found"));
        Return returnEntity = returnRepository.findByOrder(order);
        return convertToResponse(returnEntity);
    }

    /**
     * Validates return status transitions
     */
    private void validateReturnStatusTransition(ReturnStatus currentStatus, ReturnStatus newStatus) {
        switch (currentStatus) {
            case PENDING -> {
                // From PENDING, can go to APPROVED or REJECTED
                if (newStatus != ReturnStatus.APPROVED && newStatus != ReturnStatus.REJECTED) {
                    throw new IllegalStateException("Invalid return status transition from PENDING to " + newStatus);
                }
            }
            case APPROVED -> {
                // From APPROVED, can only go to COMPLETED
                if (newStatus != ReturnStatus.COMPLETED) {
                    throw new IllegalStateException("Invalid return status transition from APPROVED to " + newStatus);
                }
            }
            case REJECTED, COMPLETED -> {
                // These are terminal states
                throw new IllegalStateException("Cannot change return status from terminal state " + currentStatus);
            }
        }
    }

    private Return convertToEntity(ReturnRequest dto) {
        User user = userRepository.getByUserId(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findByOrderId(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return Return.builder()
                .user(user)
                .order(order)
                .reason(dto.getReason())
                .build();
    }

    private ReturnResponse convertToResponse(Return entity) {
        return ReturnResponse.builder()
                .returnId(entity.getRequestId())
                .userId(entity.getUser().getUserId())
                .orderId(entity.getOrder().getOrderId())
                .reason(entity.getReason())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .processedAt(entity.getProcessedAt())
                .comments(entity.getComments())
                .build();
    }
}
