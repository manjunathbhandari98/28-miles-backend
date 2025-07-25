package com.quodex._miles.service.Impl;

import com.quodex._miles.entity.Category;
import com.quodex._miles.exception.AlreadyExistsException;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.CategoryRequest;
import com.quodex._miles.io.CategoryResponse;
import com.quodex._miles.repository.CategoryRepository;
import com.quodex._miles.service.CategoryService;
import com.quodex._miles.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse addCategory(CategoryRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        String name = request.getName().trim();
        String slug = (request.getSlug() == null || request.getSlug().isEmpty())
                ? SlugUtil.toSlug(name)
                : SlugUtil.toSlug(request.getSlug());

        if (categoryRepository.existsBySlug(slug)) {
            throw new AlreadyExistsException("Category slug already exists.");
        } else{
        request.setSlug(slug);

        }

        Category category = convertToEntity(request);

        Category saved = categoryRepository.save(category);

        return convertToDTO(saved);

    }


    @Override
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream().map(
                this::convertToDTO
        ).toList();
    }

    @Override
    public CategoryResponse getCategoryByCategoryId(String categoryId) {
        Category category = categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category Not found"));
        return convertToDTO(category);
    }

    @Override
    public CategoryResponse updateCategory(String categoryId, CategoryRequest request) {
        Category category = categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category Not Found"));
        category.setCategoryName(request.getName());
        String slug = (request.getSlug() == null || request.getSlug().isEmpty())
                ? SlugUtil.toSlug(request.getName())
                : SlugUtil.toSlug(request.getSlug());


        if (categoryRepository.existsBySlug(slug)) {
            throw new AlreadyExistsException("Category slug already exists.");
        } else{
            category.setSlug(slug);
        }

        category = categoryRepository.save(category);
        CategoryResponse updatedCategory = convertToDTO(category);
        return updatedCategory;

    }

    @Override
    public void deleteCategory(String categoryId) {
        Category category = categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category Not Found"));
        categoryRepository.delete(category);
    }


    //    convert Entity to Response to send to client
    private CategoryResponse convertToDTO(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getCategoryName())
                .slug(category.getSlug())
                .build();
    }


//    Convert Request to Entity to save in Database
    private Category convertToEntity(CategoryRequest request) {
        return Category.builder()
                .categoryName(request.getName())
                .slug(request.getSlug())
                .build();
    }
}


