package com.quodex._miles.controller;

import com.quodex._miles.entity.Cart;
import com.quodex._miles.io.CartRequest;
import com.quodex._miles.io.CartResponse;
import com.quodex._miles.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/cart")
@RestController
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> addToCart(@RequestBody CartRequest request){
        CartResponse response = cartService.addToCart(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCartByUser(@PathVariable String userId){
        CartResponse response = cartService.getCartByUser(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/guest/{cartId}")
    public ResponseEntity<CartResponse> getCartByCartId(@PathVariable String cartId){
        CartResponse response = cartService.getCartByCartId(cartId);
        return ResponseEntity.ok(response);

    }

    @GetMapping()
    public ResponseEntity<List<CartResponse>> getCartItems(){
        List<CartResponse> response = cartService.getCartItems();
        return ResponseEntity.ok(response);

    }
    @PutMapping("/{cartId}")
    public ResponseEntity<CartResponse> updateCart(@PathVariable String cartId,
                                                   @RequestBody CartRequest request){
        CartResponse response = cartService.updateCart(cartId, request);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/update-quantity")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @RequestParam String cartId,
            @RequestParam String productId,
            @RequestParam String size,
            @RequestParam String color,
            @RequestParam int quantity
    ){
        CartResponse response = cartService.updateCartItemQuantity(cartId, productId, size, color, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<String> deleteCart(@PathVariable String cartId){
        cartService.deleteCart(cartId);
        return ResponseEntity.ok("Cart Deleted");
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<String> deleteCartItem(@PathVariable String cartItemId){
        cartService.deleteCartItem(cartItemId);
        return ResponseEntity.ok("Cart Item Deleted");
    }

    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeGuestCartWithUserCart(
            @RequestParam String guestCartId,
            @RequestParam String userId
    ) {
        CartResponse mergedCart = cartService.mergeGuestCartWithUserCart(guestCartId, userId);
        return ResponseEntity.ok(mergedCart);
    }

}
