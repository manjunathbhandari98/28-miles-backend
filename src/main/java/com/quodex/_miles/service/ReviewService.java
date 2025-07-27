package com.quodex._miles.service;

import com.quodex._miles.io.ReviewRequest;
import com.quodex._miles.io.ReviewResponse;

public interface ReviewService {
    ReviewResponse addReview(ReviewRequest request);

    void deleteReview(String reviewId);
}
