package com.quodex._miles.service;

import com.quodex._miles.io.CartRequest;
import com.quodex._miles.io.CartResponse;

import java.util.List;

public interface CartService {
    CartResponse addToCart(CartRequest request);
    CartResponse getCartByUser(String userId);
    CartResponse updateCart(String cartId,CartRequest request);
    void deleteCart(String cartId);
    List<CartResponse> getCartItems();
    CartResponse getCartByCartId(String cartId);
    CartResponse updateCartItemQuantity(String cartId, String productId, String size, String color, int newQuantity);

    CartResponse mergeGuestCartWithUserCart(String guestCartId, String userId);

    void deleteCartItem(String cartItemId);
    void deleteCartByUser(String userId);
}
