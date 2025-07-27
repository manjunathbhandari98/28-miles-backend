package com.quodex._miles.io;

import com.quodex._miles.constant.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnResponse {
    private String returnId;
    private String orderId;
    private String orderItemId;
    private String userId;
    private String reason;
    private ReturnStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String comments;
}
