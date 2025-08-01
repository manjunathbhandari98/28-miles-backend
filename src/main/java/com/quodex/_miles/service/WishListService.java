package com.quodex._miles.service;

import com.quodex._miles.io.WishListRequest;
import com.quodex._miles.io.WishListResponse;

public interface WishListService {

    WishListResponse addToWishList(WishListRequest request);

    WishListResponse getWishListByUser(String userId);

    void deleteWishList(String wishListId);

    WishListResponse removeProductFromWishList(String userId, String productId);

    boolean isProductInWishList(String userId, String productId);
}