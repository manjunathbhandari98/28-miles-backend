package com.quodex._miles.repository;

import com.quodex._miles.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryId(String categoryId);
    
    boolean existsBySlug(String slug);

}
