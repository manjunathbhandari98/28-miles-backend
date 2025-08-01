package com.quodex._miles.repository;

import com.quodex._miles.constant.Gender;
import com.quodex._miles.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryId(String categoryId);
    
    boolean existsBySlug(String slug);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.gender = :gender")
    List<Category> findDistinctCategoriesByGender(@Param("gender") Gender gender);


    Optional<Category> findBySlug(String slug);
}
