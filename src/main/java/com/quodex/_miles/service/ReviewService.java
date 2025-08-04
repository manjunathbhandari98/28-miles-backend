package com.quodex._miles.service;

import com.quodex._miles.io.ReviewRequest;
import com.quodex._miles.io.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
    ReviewResponse addReview(String productId, ReviewRequest request, List<MultipartFile> files);


    void deleteReview(String reviewId);

    ReviewResponse getReviewById(String reviewId);

    Page<ReviewResponse> getReviewByProduct(String productId, int page, int size);
}
