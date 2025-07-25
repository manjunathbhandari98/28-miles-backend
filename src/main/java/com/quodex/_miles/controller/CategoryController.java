package com.quodex._miles.controller;

import com.quodex._miles.io.CategoryRequest;
import com.quodex._miles.io.CategoryResponse;
import com.quodex._miles.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/category")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> addCategory(@RequestBody CategoryRequest categoryRequest) {
        CategoryResponse response = categoryService.addCategory(categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(){
        List<CategoryResponse> responses = categoryService.getCategories();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryByCategoryId(@PathVariable String categoryId) {
        CategoryResponse category = categoryService.getCategoryByCategoryId(categoryId);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/update/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable String categoryId,
                                                           @RequestBody CategoryRequest request
                                                           ){
        CategoryResponse categoryResponse = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(categoryResponse);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable String categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category Deleted Successfully");
    }


}
