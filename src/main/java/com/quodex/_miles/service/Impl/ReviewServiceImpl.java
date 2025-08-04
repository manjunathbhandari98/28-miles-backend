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
import com.quodex._miles.service.FileUploadService;
import com.quodex._miles.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    @Override
    public ReviewResponse addReview(String productId, ReviewRequest request, List<MultipartFile> files
) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        User user = userRepository.getByUserId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Reviews review =  Reviews.builder()
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .product(product)
                .build();

        // Handle file upload to Cloudinary if present
        if (files != null && !files.isEmpty()) {
            List<String> imgUrls = files.stream()
                    .map(fileUploadService::uploadFile)
                    .collect(Collectors.toList());

            review.setImages(imgUrls);
        }


        Reviews savedReview = reviewRepository.save(review);

        // Update product rating after saving review
        updateProductRating(product);

        return convertToResponse(savedReview);
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

    @Override
    public Page<ReviewResponse> getReviewByProduct(String productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Reviews> reviewPage = reviewRepository.findByProduct_ProductId(productId, pageable);
        return reviewPage.map(this::convertToResponse);
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
                .images(reviews.getImages())
                .build();
    }



    private void updateProductRating(Product product) {
        List<Reviews> allReviews = reviewRepository.findByProduct(product);
        double averageRating = allReviews.stream()
                .mapToDouble(Reviews::getRating)
                .average()
                .orElse(0.0);
        BigDecimal rounded = new BigDecimal(averageRating).setScale(1, RoundingMode.HALF_UP);
        product.setRating(rounded.doubleValue());

        productRepository.save(product); // Save updated rating
    }

}

