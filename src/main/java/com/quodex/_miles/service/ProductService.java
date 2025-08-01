package com.quodex._miles.service;

import com.quodex._miles.constant.Gender;
import com.quodex._miles.io.ProductRequest;
import com.quodex._miles.io.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ProductResponse addProduct(
            @RequestPart("product") ProductRequest request,
            @RequestPart("files") List<MultipartFile> files
    );

    Page<ProductResponse> getAllProducts(int page, int size);

    Page<ProductResponse> getProductsByFilters(Gender gender, String categoryId, int page, int size);

    ProductResponse getProductById(String productId);

    ProductResponse updateProduct(String productId, ProductRequest request, List<MultipartFile> files);

    void deleteProduct(String productId);

    ProductResponse getProductBySlug(String slug);

    Page<ProductResponse> getNewArrivals(int page, int size);
}
