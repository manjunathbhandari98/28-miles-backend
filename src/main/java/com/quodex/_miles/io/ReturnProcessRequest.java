package com.quodex._miles.io;

import com.quodex._miles.constant.ReturnStatus;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnProcessRequest {
    private ReturnStatus status;
    private LocalDateTime processedAt;
    private String comments;

}
