package com.quodex._miles.repository;

import com.quodex._miles.entity.Product;
import com.quodex._miles.entity.Reviews;
import com.quodex._miles.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Reviews, Long> {
    List<Reviews> findByProduct(Product product);

    Optional<Reviews> findByUserAndProduct(User user, Product product);

    Optional<Reviews> findByReviewId(String reviewId);
}
