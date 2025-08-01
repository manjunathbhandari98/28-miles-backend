package com.quodex._miles.service;

import com.quodex._miles.constant.Gender;
import com.quodex._miles.io.CategoryRequest;
import com.quodex._miles.io.CategoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {
    CategoryResponse addCategory(CategoryRequest categoryRequest, MultipartFile file);

    List<CategoryResponse> getCategories();

    List<CategoryResponse> getCategoriesByGender(Gender gender);

    CategoryResponse getCategoryByCategoryId(String categoryId);

    CategoryResponse updateCategory(String categoryId, CategoryRequest request, MultipartFile file);

    void deleteCategory(String categoryId);

    CategoryResponse getCategoriesBySlug(String slug);
}
