package com.quodex._miles.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse {
    private String reviewId;
    private String userId;
    private String username;
    private String productId;
    private  double rating;
    private String comment;
    private LocalDateTime createdAt;
}
