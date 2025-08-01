package com.quodex._miles.controller;

import com.quodex._miles.io.WishListRequest;
import com.quodex._miles.io.WishListResponse;
import com.quodex._miles.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/wishlist")
@RestController
@RequiredArgsConstructor
public class WishListController {
    private final WishListService wishListService;

    @PostMapping()
    public ResponseEntity<WishListResponse> addToWishlist(@RequestBody WishListRequest request) {
        try {
            WishListResponse response = wishListService.addToWishList(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WishListResponse> getWishList(@PathVariable String userId) {
        try {
            WishListResponse response = wishListService.getWishListByUser(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Fixed: This should remove the entire wishlist (if that's the intention)
    @DeleteMapping("/{wishListId}")
    public ResponseEntity<String> deleteWishList(@PathVariable String wishListId) {
        try {
            wishListService.deleteWishList(wishListId);
            return ResponseEntity.ok("Wishlist Deleted Successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Wishlist not found");
        }
    }

    // Better approach: Remove specific product from wishlist
    @DeleteMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<WishListResponse> removeProductFromWishList(
            @PathVariable String userId,
            @PathVariable String productId) {
        try {
            WishListResponse response = wishListService.removeProductFromWishList(userId, productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Check if product is in wishlist
    @GetMapping("/user/{userId}/product/{productId}/exists")
    public ResponseEntity<Boolean> isProductInWishList(
            @PathVariable String userId,
            @PathVariable String productId) {
        boolean exists = wishListService.isProductInWishList(userId, productId);
        return ResponseEntity.ok(exists);
    }

    // Toggle product in wishlist (add if not present, remove if present)
    @PostMapping("/user/{userId}/product/{productId}/toggle")
    public ResponseEntity<WishListResponse> toggleProductInWishList(
            @PathVariable String userId,
            @PathVariable String productId) {
        try {
            if (wishListService.isProductInWishList(userId, productId)) {
                WishListResponse response = wishListService.removeProductFromWishList(userId, productId);
                return ResponseEntity.ok(response);
            } else {
                WishListRequest request = new WishListRequest();
                request.setUserId(userId);
                request.setProductId(productId);
                WishListResponse response = wishListService.addToWishList(request);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}