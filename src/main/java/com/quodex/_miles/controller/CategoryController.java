package com.quodex._miles.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quodex._miles.constant.Gender;
import com.quodex._miles.io.CategoryRequest;
import com.quodex._miles.io.CategoryResponse;
import com.quodex._miles.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.cloudinary.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/category")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> addCategory(@RequestPart("category") String category,
                                                        @RequestPart("image")MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        CategoryRequest categoryRequest;
        try{
            categoryRequest = objectMapper.readValue(category, CategoryRequest.class);
            CategoryResponse response =  categoryService.addCategory(categoryRequest, file);
            return ResponseEntity.ok(response);
        }  catch (RuntimeException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(){
        List<CategoryResponse> responses = categoryService.getCategories();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/slug")
    public ResponseEntity<CategoryResponse> getCategoriesBySlug(@RequestParam String slug){
        CategoryResponse responses = categoryService.getCategoriesBySlug(slug);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/gender")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByGender(@RequestParam Gender gender){
        List<CategoryResponse> responses = categoryService.getCategoriesByGender(gender);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryByCategoryId(@PathVariable String categoryId) {
        CategoryResponse category = categoryService.getCategoryByCategoryId(categoryId);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/update/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable String categoryId,
                                                           @RequestPart("category") String category,
                                                           @RequestPart(value = "image", required = false) MultipartFile file
                                                           ){
        ObjectMapper objectMapper = new ObjectMapper();
        CategoryRequest categoryRequest;
        try{
            categoryRequest = objectMapper.readValue(category, CategoryRequest.class);
            CategoryResponse response  = categoryService.updateCategory(categoryId, categoryRequest, file);
            return ResponseEntity.ok(response);
        }  catch (RuntimeException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable String categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category Deleted Successfully");
    }


}
