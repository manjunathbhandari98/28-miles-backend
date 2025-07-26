package com.quodex._miles.service.Impl;

import com.quodex._miles.entity.Cart;
import com.quodex._miles.entity.CartItem;
import com.quodex._miles.entity.User;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.CartItemRequest;
import com.quodex._miles.io.CartItemResponse;
import com.quodex._miles.io.CartRequest;
import com.quodex._miles.io.CartResponse;
import com.quodex._miles.repository.CartRepository;
import com.quodex._miles.repository.UserRepository;
import com.quodex._miles.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponse addToCart(CartRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.getByUserId(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        Cart cart;

        if (user != null) {
            // Logged-in user
            cart = cartRepository.findByUser_UserId(user.getUserId())
                    .orElse(Cart.builder().user(user).items(new ArrayList<>()).build());
        } else if (request.getCartId() != null) {
            // Guest user
            cart = cartRepository.getCartByCartId(request.getCartId())
                    .orElse(Cart.builder().items(new ArrayList<>()).build());
        } else {
            throw new IllegalArgumentException("Either userId or cartId must be provided");
        }

        for (CartItemRequest itemRequest : request.getItems()) {
            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(ci -> ci.getProductId().equals(itemRequest.getProductId())
                            && ci.getSize().equals(itemRequest.getSize())
                            && ci.getColor().equals(itemRequest.getColor()))
                    .findFirst();

            if (existingItem.isPresent()) {
                CartItem cartItem = existingItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + itemRequest.getQuantity());
            } else {
                CartItem newItem = convertToCartItemEntity(itemRequest, cart);
                cart.getItems().add(newItem);
            }
        }

        Cart savedCart = cartRepository.save(cart);
        return convertToCartResponse(savedCart);
    }

    @Override
    public CartResponse updateCart(String cartId, CartRequest request) {
        Cart cart = cartRepository.getCartByCartId(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Not Found"));

        cart.getItems().clear();

        for (CartItemRequest itemRequest : request.getItems()) {
            CartItem newItem = convertToCartItemEntity(itemRequest, cart);
            cart.getItems().add(newItem);
        }

        Cart updatedCart = cartRepository.save(cart);
        return convertToCartResponse(updatedCart);
    }

    @Override
    public List<CartResponse> getCartByUser(String userId) {
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return cartRepository.findByUser_UserId(userId).stream()
                .map(this::convertToCartResponse).toList();
    }

    @Override
    public void deleteCart(String cartId) {
        Cart cart = cartRepository.getCartByCartId(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Not Found"));
        cartRepository.delete(cart);
    }

    @Override
    public List<CartResponse> getCartItems() {
        return cartRepository.findAll().stream()
                .map(this::convertToCartResponse)
                .toList();
    }

    @Override
    public CartResponse mergeGuestCartWithUserCart(String guestCartId, String userId) {
        Cart guestCart = cartRepository.getCartByCartId(guestCartId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest Cart Not Found"));
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Cart userCart = cartRepository.findByUser_UserId(userId)
                .orElse(Cart.builder().user(user).items(new ArrayList<>()).build());

        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> existingItem = userCart.getItems().stream()
                    .filter(ci -> ci.getProductId().equals(guestItem.getProductId())
                            && ci.getSize().equals(guestItem.getSize())
                            && ci.getColor().equals(guestItem.getColor()))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + guestItem.getQuantity());
            } else {
                CartItem newItem = CartItem.builder()
                        .productId(guestItem.getProductId())
                        .productName(guestItem.getProductName())
                        .size(guestItem.getSize())
                        .color(guestItem.getColor())
                        .quantity(guestItem.getQuantity())
                        .price(guestItem.getPrice())
                        .image(guestItem.getImage())
                        .cart(userCart)
                        .build();
                userCart.getItems().add(newItem);
            }
        }

        Cart mergedCart = cartRepository.save(userCart);
        cartRepository.delete(guestCart);

        return convertToCartResponse(mergedCart);
    }

    private CartItem convertToCartItemEntity(CartItemRequest request, Cart cart) {
        return CartItem.builder()
                .cart(cart)
                .productId(request.getProductId())
                .productName(request.getProductName())
                .size(request.getSize())
                .color(request.getColor())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .image(request.getImage())
                .build();
    }

    private CartItemResponse convertToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .size(item.getSize())
                .color(item.getColor())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .image(item.getImage())
                .build();
    }

    private CartResponse convertToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUser() != null ? cart.getUser().getUserId() : null)
                .items(items)
                .build();
    }
}
