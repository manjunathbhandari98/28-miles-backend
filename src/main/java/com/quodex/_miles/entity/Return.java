package com.quodex._miles.entity;

import com.quodex._miles.constant.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestId; // e.g., RET-20250725-001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String reason;

    private ReturnStatus status; // e.g., PENDING, APPROVED, REJECTED, COMPLETED

    private LocalDateTime requestedAt;

    private LocalDateTime processedAt;

    private String comments; // admin notes or processing comments

    @PrePersist
    public void prePersist(){
        if(this.requestId == null){
            this.requestId ="RET"+ UUID.randomUUID().toString().toUpperCase().substring(0,7);
        }
        if(this.requestedAt == null){
            this.requestedAt = LocalDateTime.now();
        }
        if (this.status == null){
            this.status = ReturnStatus.PENDING;
        }
    }
}
