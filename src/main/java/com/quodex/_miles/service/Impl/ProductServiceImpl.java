package com.quodex._miles.service.Impl;

import com.quodex._miles.entity.Category;
import com.quodex._miles.entity.Product;
import com.quodex._miles.entity.Reviews;
import com.quodex._miles.exception.AlreadyExistsException;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.ProductRequest;
import com.quodex._miles.io.ProductResponse;
import com.quodex._miles.io.ReviewResponse;
import com.quodex._miles.repository.CategoryRepository;
import com.quodex._miles.repository.ProductRepository;
import com.quodex._miles.repository.ReviewRepository;
import com.quodex._miles.service.ProductService;
import com.quodex._miles.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public ProductResponse addProduct(ProductRequest request) {
        String slug = (request.getSlug() == null || request.getSlug().isEmpty())
                ? SlugUtil.toSlug(request.getName())
                : SlugUtil.toSlug(request.getSlug());

        if (productRepository.existsBySlug(slug)) {
            throw new AlreadyExistsException("Product Already Exists");
        }

        Category category = categoryRepository.findByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category Not Found"));

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .category(category)
                .colors(request.getColors())
                .sizes(request.getSizes())
                .tags(request.getTags())
                .images(request.getImages())
                .description(request.getDescription())
                .price(request.getPrice())
                .oldPrice(request.getOldPrice())
                .material(request.getMaterial())
                .gender(request.getGender())
                .stock(request.getStock())
                .isTrending(request.getIsTrending())
                .productFeatures(request.getProductFeatures())
                .rating(0.0) // Initially no rating
                .build();

        Product savedProduct = productRepository.save(product);
        return convertToResponse(savedProduct);
    }

    @Override
    public List<ProductResponse> getProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
        return convertToResponse(product);
    }

    @Override
    public ProductResponse updateProduct(String productId, ProductRequest request) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));

        String slug = (request.getSlug() == null || request.getSlug().isEmpty())
                ? SlugUtil.toSlug(request.getName())
                : SlugUtil.toSlug(request.getSlug());

        if (!slug.equals(product.getSlug()) && productRepository.existsBySlug(slug)) {
            throw new AlreadyExistsException("Product with this slug already exists");
        }

        Category category = categoryRepository.findByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category Not Found"));

        product.setName(request.getName());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOldPrice(request.getOldPrice());
        product.setColors(request.getColors());
        product.setSizes(request.getSizes());
        product.setTags(request.getTags());
        product.setImages(request.getImages());
        product.setProductFeatures(request.getProductFeatures());
        product.setMaterial(request.getMaterial());
        product.setGender(request.getGender());
        product.setStock(request.getStock());
        product.setIsTrending(request.getIsTrending());
        product.setCategory(category);

        // Don't set reviews here. Rating will be updated separately.
        Product updatedProduct = productRepository.save(product);
        return convertToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
        productRepository.delete(product);
    }

    private ProductResponse convertToResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .oldPrice(product.getOldPrice())
                .rating(product.getRating())
                .gender(product.getGender())
                .material(product.getMaterial())
                .stock(product.getStock())
                .isTrending(product.getIsTrending())
                .sizes(product.getSizes())
                .colors(product.getColors())
                .tags(product.getTags())
                .images(product.getImages())
                .categoryId(product.getCategory().getCategoryId())
                .productFeatures(product.getProductFeatures())
                .reviews(mapReviews(reviewRepository.findByProduct(product)))
// return reviews only for response
                .build();
    }

    private List<ReviewResponse> mapReviews(List<Reviews> reviews) {
        return reviews.stream()
                .map(review -> ReviewResponse.builder()
                        .reviewId(review.getReviewId())
                        .comment(review.getComment())
                        .rating(review.getRating())
                        .userId(review.getUser().getUserId())
                        .productId(review.getProduct().getProductId())
                        .build())
                .toList();
    }

}

