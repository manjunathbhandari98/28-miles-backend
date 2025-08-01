package com.quodex._miles.service.Impl;

import com.quodex._miles.entity.Product;
import com.quodex._miles.entity.Reviews;
import com.quodex._miles.entity.User;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.ReviewRequest;
import com.quodex._miles.io.ReviewResponse;
import com.quodex._miles.repository.ProductRepository;
import com.quodex._miles.repository.ReviewRepository;
import com.quodex._miles.repository.UserRepository;
import com.quodex._miles.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewResponse addReview(ReviewRequest request) {
        Product product = productRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User user = userRepository.getByUserId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if the user already reviewed the product
        Reviews existingReview = reviewRepository.findByUserAndProduct(user, product).orElse(null);

        if (existingReview != null) {
            // Update the existing review
            existingReview.setComment(request.getComment());
            existingReview.setRating(request.getRating());
            reviewRepository.save(existingReview);
        } else {
            // Create new review
            Reviews review = Reviews.builder()
                    .user(user)
                    .product(product)
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .build();
            reviewRepository.save(review);
        }

        // Recalculate product's overall rating
        updateProductRating(product);

        // Return updated/created review
        Reviews saved = reviewRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ResourceNotFoundException("Unexpected error retrieving review"));

        return ReviewResponse.builder()
                .reviewId(saved.getReviewId())
                .productId(product.getProductId())
                .userId(user.getUserId())
                .rating(saved.getRating())
                .comment(saved.getComment())
                .username(user.getName())
                .build();
    }

    @Override
    public void deleteReview(String reviewId){
        Reviews reviews = reviewRepository.findByReviewId(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review Not Found"));
        reviewRepository.delete(reviews);
    }

    @Override
    public ReviewResponse getReviewById(String reviewId) {
        Reviews reviews = reviewRepository.findByReviewId(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review Not Found"));
        return convertToResponse(reviews);
    }

    private ReviewResponse convertToResponse(Reviews reviews) {
        return ReviewResponse.builder()
                .reviewId(reviews.getReviewId())
                .username(reviews.getUser().getName())
                .rating(reviews.getRating())
                .userId(reviews.getUser().getUserId())
                .comment(reviews.getComment())
                .productId(reviews.getProduct().getProductId())
                .createdAt(reviews.getCreatedAt())
                .build();
    }


    private void updateProductRating(Product product) {
        List<Reviews> allReviews = reviewRepository.findByProduct(product);
        double averageRating = allReviews.stream()
                .mapToDouble(Reviews::getRating)
                .average()
                .orElse(0.0);
        double roundedRating = Math.round(averageRating * 10.0)/10.0;
        product.setRating(roundedRating);
        productRepository.save(product); // Save updated rating
    }

}

