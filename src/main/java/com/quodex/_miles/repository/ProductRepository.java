package com.quodex._miles.repository;

import com.quodex._miles.constant.Gender;
import com.quodex._miles.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySlug(String slug);

    Page<Product> findAll(Pageable pageable);
    Page<Product> findByGender(Gender gender, Pageable pageable);

    Page<Product> findByCategory_Slug(String categorySlug, Pageable pageable);

    Page<Product> findByGenderAndCategory_Slug(Gender gender, String categorySlug, Pageable pageable);


    Optional<Product> findByProductId(String productId);


    Optional<Product> findBySlug(String slug);

    @Query("SELECT p FROM Product p WHERE p.createdAt >= :startDate")
    Page<Product> findNewArrivals(@Param("startDate") LocalDateTime startDate, Pageable pageable);

}
