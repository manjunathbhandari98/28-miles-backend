package com.quodex._miles.service;

import com.quodex._miles.io.CategoryRequest;
import com.quodex._miles.io.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse addCategory(CategoryRequest categoryRequest);

    List<CategoryResponse> getCategories();

    CategoryResponse getCategoryByCategoryId(String categoryId);

    CategoryResponse updateCategory(String categoryId, CategoryRequest request);

    void deleteCategory(String categoryId);
}
