package com.quodex._miles.service.Impl;

import com.quodex._miles.entity.Cart;
import com.quodex._miles.entity.CartItem;
import com.quodex._miles.entity.Product;
import com.quodex._miles.entity.User;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.CartItemRequest;
import com.quodex._miles.io.CartItemResponse;
import com.quodex._miles.io.CartRequest;
import com.quodex._miles.io.CartResponse;
import com.quodex._miles.repository.CartRepository;
import com.quodex._miles.repository.ProductRepository;
import com.quodex._miles.repository.UserRepository;
import com.quodex._miles.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);


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
                    .orElse(Cart.builder()
                            .cartId(request.getCartId())
                            .items(new ArrayList<>())
                            .build());
        } else {
            throw new IllegalArgumentException("Either userId or cartId must be provided");
        }

        // Check if this is a new cart (no ID assigned yet)
        boolean isNewCart = cart.getId() == null;

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

        calculateCartTotals(cart);

        // Save the cart first to ensure createdAt is set
        Cart savedCart = cartRepository.save(cart);

        // Set expected date only for new carts or if expected date is not set
        if (isNewCart || savedCart.getExceptedDate() == null) {
            LocalDateTime expectedDate = savedCart.getCreatedAt().plusDays(10);
            savedCart.setExceptedDate(expectedDate);
            savedCart = cartRepository.save(savedCart);
        }

        return convertToCartResponse(savedCart);
    }
    @Override
    public CartResponse updateCartItemQuantity(String cartId, String productId, String size, String color, int newQuantity) {
        Cart cart = cartRepository.getCartByCartId(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Not Found"));

        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId)
                        && item.getSize().equalsIgnoreCase(size)
                        && item.getColor().equalsIgnoreCase(color))
                .findFirst();

        if (itemOpt.isEmpty()) {
            throw new ResourceNotFoundException("Cart item not found with specified product, size, and color");
        }

        CartItem item = itemOpt.get();

        if (newQuantity <= 0) {
            // Optionally remove item if quantity is set to zero
            cart.getItems().remove(item);
        } else {
            item.setQuantity(newQuantity);
        }

        calculateCartTotals(cart);
        Cart updatedCart = cartRepository.save(cart);

        return convertToCartResponse(updatedCart);
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
        calculateCartTotals(cart);
        Cart updatedCart = cartRepository.save(cart);
        return convertToCartResponse(updatedCart);
    }

    @Override
    public CartResponse getCartByUser(String userId) {
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        Cart cart =  cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Not Found"));

        return convertToCartResponse(cart);
    }

    @Override
    public CartResponse getCartByCartId(String cartId){
        Cart cart = cartRepository.getCartByCartId(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Not Found"));
        return convertToCartResponse(cart);
    }

    @Override
    public void deleteCart(String cartId) {
        Cart cart = cartRepository.getCartByCartId(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Not Found"));
        cartRepository.delete(cart);
    }

    @Override
    public void deleteCartItem(String cartItemId) {
        Cart cart = cartRepository.findAll().stream()
                .filter(c -> c.getItems().stream()
                        .anyMatch(i -> i.getCartItemId().equals(cartItemId)))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for cartItemId: " + cartItemId));

        CartItem itemToRemove = cart.getItems().stream()
                .filter(i -> i.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        cart.getItems().remove(itemToRemove);

        calculateCartTotals(cart); // update totals after removal
        cartRepository.save(cart); // persist changes
    }


    @Override
    public List<CartResponse> getCartItems() {
        return cartRepository.findAll().stream()
                .map(this::convertToCartResponse)
                .toList();
    }



    @Transactional
    @Override
    public CartResponse mergeGuestCartWithUserCart(String guestCartId, String userId) {
        log.info("Starting cart reassignment. GuestCartId: {}, UserId: {}", guestCartId, userId);

        Cart guestCart = cartRepository.getCartByCartId(guestCartId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest Cart Not Found with ID: " + guestCartId));

        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with ID: " + userId));

        // Attach user to the guest cart
        guestCart.setUser(user);

        // Generate a new cartId
        String newCartId = "CART" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);
        guestCart.setCartId(newCartId);

        calculateCartTotals(guestCart);
        // Save updated cart
        Cart updatedCart = cartRepository.save(guestCart);

        log.info("Cart reassigned and updated with new ID: {}", newCartId);

        return convertToCartResponse(updatedCart);
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
                .oldPrice(request.getOldPrice())
                .tax(request.getTax())
                .image(request.getImage())
                .build();
    }

    private CartItemResponse convertToCartItemResponse(CartItem item) {
        Optional<Product> productOpt = productRepository.findByProductId(item.getProductId());
        if (productOpt.isEmpty()) {
            return null;
        }
        Product product = productOpt.get();

        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .category(product.getCategory().getCategoryName())
                .size(item.getSize())
                .color(item.getColor())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .oldPrice(item.getOldPrice())
                .image(item.getImage())
                .tax(item.getTax())
                .build();
    }

    private CartResponse convertToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::convertToCartItemResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        cart.setCreatedAt(LocalDateTime.now());
        LocalDateTime expectedDate = cart.getExceptedDate();
        if (expectedDate == null && cart.getCreatedAt() != null) {
            expectedDate = cart.getCreatedAt().plusDays(10);
        }

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUser() != null ? cart.getUser().getUserId() : null)
                .items(items)
                .createdAt(cart.getCreatedAt())
                .expectedDate(expectedDate)
                .subTotal(cart.getSubTotal())
                .totalDiscount(cart.getTotalDiscount())
                .totalTax(cart.getTotalTax())
                .grandTotal(cart.getGrandTotal())
                .build();
    }

    private void calculateCartTotals(Cart cart) {
        double subTotal = 0.0;
        double discount = 0.0;
        double totalTax = 0.0;

        for (CartItem item : cart.getItems()) {
            double itemTotalOldPrice = item.getOldPrice() * item.getQuantity();
            double itemTotalPrice = item.getPrice() * item.getQuantity();
            double itemTotalTax = item.getTax() * item.getQuantity();

            subTotal += itemTotalOldPrice;
            discount += (itemTotalOldPrice - itemTotalPrice);
            totalTax += itemTotalTax;
        }

        cart.setSubTotal(subTotal);
        cart.setTotalDiscount(discount);
        cart.setTotalTax(totalTax);
        cart.setGrandTotal(subTotal - discount );
    }


}
