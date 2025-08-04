package com.quodex._miles.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "reviews")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private double rating; // Typically 1 to 5

    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt;

    private List<String> images;

    @PrePersist
    public void prePersist() {
        this.reviewId = "REV" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);
        this.createdAt = LocalDateTime.now();
    }
}
