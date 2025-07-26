package com.quodex._miles.controller;

import com.quodex._miles.io.WishListRequest;
import com.quodex._miles.io.WishListResponse;
import com.quodex._miles.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/wishlist")
@RestController
@RequiredArgsConstructor
public class WishListController {
    private final WishListService wishListService;

    @PostMapping()
    public ResponseEntity<WishListResponse> addToWishlist(@RequestBody WishListRequest request){
        WishListResponse response = wishListService.addToWishList(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WishListResponse> getWishList(@PathVariable String userId){
        WishListResponse response = wishListService.getWishListByUser(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{wishListId}")
    public ResponseEntity<String> deleteWishList(@PathVariable String wishListId){
        wishListService.deleteWishList(wishListId);
        return ResponseEntity.ok("Wishlist Deleted");
    }
}
