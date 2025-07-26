package com.quodex._miles.service;

import com.quodex._miles.io.CartRequest;
import com.quodex._miles.io.CartResponse;

import java.util.List;

public interface CartService {
    CartResponse addToCart(CartRequest request);
    List<CartResponse> getCartByUser(String userId);
    CartResponse updateCart(String cartId,CartRequest request);
    void deleteCart(String cartId);
    List<CartResponse> getCartItems();

    CartResponse mergeGuestCartWithUserCart(String guestCartId, String userId);
}
