package com.quodex._miles.controller;

import com.quodex._miles.io.ProductRequest;
import com.quodex._miles.io.ProductResponse;
import com.quodex._miles.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/products")
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> addProducts(@RequestBody ProductRequest request){
        ProductResponse productResponse = productService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(){
        List<ProductResponse> productResponse = productService.getProducts();
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String productId){
        ProductResponse productResponse = productService.getProductById(productId);
        return ResponseEntity.ok(productResponse);
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String productId,
                                                        @RequestBody ProductRequest request){
        ProductResponse productResponse = productService.updateProduct(productId, request);
        return ResponseEntity.ok(productResponse);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable String productId){
        productService.deleteProduct(productId);
        return ResponseEntity.ok("Product Deleted Successfully");
    }
}
