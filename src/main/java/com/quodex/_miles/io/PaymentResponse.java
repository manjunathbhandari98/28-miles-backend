package com.quodex._miles.io;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
        private String id;
        private String entity;
        private String currency;
        private Integer amount;
        private String status;
        private LocalDateTime createdAt;
        private String receipt;
}
