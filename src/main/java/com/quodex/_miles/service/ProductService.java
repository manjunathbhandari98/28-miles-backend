package com.quodex._miles.service;

import com.quodex._miles.io.ProductRequest;
import com.quodex._miles.io.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse addProduct(ProductRequest request);

    List<ProductResponse> getProducts();

    ProductResponse getProductById(String productId);

    ProductResponse updateProduct(String productId, ProductRequest request);

    void deleteProduct(String productId);
}
