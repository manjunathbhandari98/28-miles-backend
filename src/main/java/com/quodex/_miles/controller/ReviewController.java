package com.quodex._miles.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quodex._miles.io.ReviewRequest;
import com.quodex._miles.io.ReviewResponse;
import com.quodex._miles.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/reviews")
@RestController
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/{productId}")
    public ReviewResponse addReview(
            @PathVariable String productId,
            @RequestPart("review") String review,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ReviewRequest reviewRequest;
            reviewRequest = objectMapper.readValue(review, ReviewRequest.class);
            return reviewService.addReview(productId, reviewRequest, files);
        } catch (RuntimeException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable String reviewId) {
        ReviewResponse response = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewByProduct(@PathVariable String productId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size)
    {
        return ResponseEntity.ok(reviewService.getReviewByProduct(productId,page, size));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }


}
