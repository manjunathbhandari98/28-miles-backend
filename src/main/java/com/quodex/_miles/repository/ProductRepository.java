package com.quodex._miles.repository;

import com.quodex._miles.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySlug(String slug);

    Optional<Product> findByProductId(String productId);
}
