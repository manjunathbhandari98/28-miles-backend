package com.quodex._miles.service.Impl;

import com.quodex._miles.entity.Product;
import com.quodex._miles.entity.Reviews;
import com.quodex._miles.entity.User;
import com.quodex._miles.entity.WishList;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.ProductResponse;
import com.quodex._miles.io.ReviewResponse;
import com.quodex._miles.io.WishListRequest;
import com.quodex._miles.io.WishListResponse;
import com.quodex._miles.repository.ProductRepository;
import com.quodex._miles.repository.UserRepository;
import com.quodex._miles.repository.WishListRepository;
import com.quodex._miles.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public WishListResponse addToWishList(WishListRequest request) {
        User user = userRepository.getByUserId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Product product = productRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));

        // Check if wishlist already exists for this user
        WishList wishList = wishListRepository.findByUser(user)
                .orElse(WishList.builder()
                        .user(user)
                        .products(new ArrayList<>())
                        .build());

        // Check for duplicates using productId comparison for better reliability
        boolean productExists = wishList.getProducts().stream()
                .anyMatch(p -> p.getProductId().equals(product.getProductId()));

        if (!productExists) {
            wishList.getProducts().add(product);
        }

        WishList saved = wishListRepository.save(wishList);
        return convertToResponse(saved);
    }

    @Override
    public WishListResponse getWishListByUser(String userId) {
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        WishList wishList = wishListRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found for user ID: " + userId));

        return convertToResponse(wishList);
    }

    @Override
    public void deleteWishList(String wishListId) {
        WishList wishList = wishListRepository.findByWishListId(wishListId);
        if (wishList == null) {
            throw new ResourceNotFoundException("WishList Not Found");
        }
        wishListRepository.delete(wishList);
    }

    // New method to remove specific product from wishlist
    @Override
    public WishListResponse removeProductFromWishList(String userId, String productId) {
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        WishList wishList = wishListRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found for user ID: " + userId));

        Product productToRemove = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));

        boolean removed = wishList.getProducts().remove(productToRemove);

        WishList saved = wishListRepository.save(wishList);
        return convertToResponse(saved);
    }


    // New method to check if product exists in wishlist
    @Override
    public boolean isProductInWishList(String userId, String productId) {
        Optional<User> userOpt = userRepository.getByUserId(userId);
        if (userOpt.isEmpty()) {
            return false;
        }

        Optional<WishList> wishListOpt = wishListRepository.findByUser(userOpt.get());
        if (wishListOpt.isEmpty()) {
            return false;
        }

        return wishListOpt.get().getProducts().stream()
                .anyMatch(product -> product.getProductId().equals(productId));
    }

    private WishListResponse convertToResponse(WishList wishlist) {
        List<ProductResponse> productResponses = wishlist.getProducts().stream()
                .map(product -> ProductResponse.builder()
                        .productId(product.getProductId())
                        .name(product.getName())
                        .slug(product.getSlug())
                        .description(product.getDescription())
                        .summary(product.getSummary())
                        .price(product.getPrice())
                        .oldPrice(product.getOldPrice())
                        .rating(product.getRating())
                        .gender(product.getGender())
                        .material(product.getMaterial())
                        .stock(product.getStock())
                        .isTrending(product.getIsTrending())
                        .sizes(product.getSizes())
                        .colors(product.getColors())
                        .tags(product.getTags())
                        .images(product.getImages())
                        .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                        .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                        .productFeatures(product.getProductFeatures())
                        .reviews(mapReviews(product.getReviews()))
                        .build()
                ).toList();

        return WishListResponse.builder()
                .wishListId(wishlist.getWishListId())
                .userId(wishlist.getUser().getUserId())
                .products(productResponses)
                .build();
    }

    private List<ReviewResponse> mapReviews(List<Reviews> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return new ArrayList<>();
        }

        return reviews.stream()
                .map(review -> ReviewResponse.builder()
                        .reviewId(review.getReviewId())
                        .comment(review.getComment())
                        .rating(review.getRating())
                        .userId(review.getUser().getUserId())
                        .productId(review.getProduct().getProductId())
                        .build())
                .toList();
    }
}