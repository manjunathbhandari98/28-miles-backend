package com.quodex._miles.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quodex._miles.constant.Gender;
import com.quodex._miles.io.ProductRequest;
import com.quodex._miles.io.ProductResponse;
import com.quodex._miles.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/products")
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;


    @PostMapping
    public ProductResponse addProducts(@RequestPart("product") String product,
                                                       @RequestPart("images") List<MultipartFile> files){
        ObjectMapper mapper = new ObjectMapper();
        ProductRequest productRequest;
        try{
            productRequest = mapper.readValue(product, ProductRequest.class);
            return  productService.addProduct(productRequest, files);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    @GetMapping()
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.getProductsByFilters(gender, categorySlug, page, size));
    }
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getSimilarProducts(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.getSimilarProducts(categoryId, page, size));
    }



    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String productId){
        ProductResponse productResponse = productService.getProductById(productId);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug){
        ProductResponse productResponse = productService.getProductBySlug(slug);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/new-arrivals")
    public Page<ProductResponse> getNewArrivals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return productService.getNewArrivals(page, size);
    }



    @PutMapping(value = "/update/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse updateProduct(
            @PathVariable String productId,
            @RequestPart("product") String request,
            @RequestPart(value = "images", required = false) List<MultipartFile> files
    ){
        ObjectMapper mapper = new ObjectMapper();
        ProductRequest productRequest;
        try{
            productRequest = mapper.readValue(request, ProductRequest.class);
            return productService.updateProduct(productId, productRequest, files);
        }
        catch (RuntimeException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable String productId){
        productService.deleteProduct(productId);
        return ResponseEntity.ok("Product Deleted Successfully");
    }
}
