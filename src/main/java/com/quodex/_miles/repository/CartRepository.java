package com.quodex._miles.repository;

import com.quodex._miles.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> getCartByCartId(String cartId);

    Optional<Cart> findByUser_UserId(String userId);
}
