package com.quodex._miles.service.Impl;

import com.quodex._miles.entity.Category;
import com.quodex._miles.entity.Product;
import com.quodex._miles.entity.Reviews;
import com.quodex._miles.exception.AlreadyExistsException;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.ProductRequest;
import com.quodex._miles.io.ProductResponse;
import com.quodex._miles.repository.CategoryRepository;
import com.quodex._miles.repository.ProductRepository;
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

    @Override
    public ProductResponse addProduct(ProductRequest request) {
        // Generate slug
        String slug = (request.getSlug() == null || request.getSlug().isEmpty())
                ? SlugUtil.toSlug(request.getName())
                : SlugUtil.toSlug(request.getSlug());

        if (productRepository.existsBySlug(slug)) {
            throw new AlreadyExistsException("Product Already Exists");
        }

        request.setSlug(slug);

        Product product = convertToEntity(request);
        productRepository.save(product);

        return convertToResponse(product);
    }

    @Override
    public List<ProductResponse> getProducts(){
        return productRepository.findAll().stream().map(
                this::convertToResponse
        ).toList();
    }

    @Override
    public ProductResponse getProductById(String productId){
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
        return convertToResponse(product);
    }

    @Override
    public ProductResponse updateProduct(String productId, ProductRequest request) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));

        // Generate or use provided slug
        String slug = (request.getSlug() == null || request.getSlug().isEmpty())
                ? SlugUtil.toSlug(request.getName())
                : SlugUtil.toSlug(request.getSlug());

        // If the slug has changed, ensure it doesn't conflict with another product
        if (!slug.equals(product.getSlug()) && productRepository.existsBySlug(slug)) {
            throw new AlreadyExistsException("Product with this slug already exists");
        }

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

        // Update category if different
        if (!product.getCategory().getCategoryId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findByCategoryId(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category Not Found"));
            product.setCategory(category);
        }

        // Optional: Update rating if reviews are supplied (or ignore this if handled separately)
        if (request.getReviews() != null && !request.getReviews().isEmpty()) {
            product.setRating(calculateAverageRating(request.getReviews()));
            product.setReviews(request.getReviews());
        }

        Product updatedProduct = productRepository.save(product);
        return convertToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(String productId){
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
        productRepository.delete(product);
    }


    private Product convertToEntity(ProductRequest request) {
        Category category = categoryRepository.findByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category Not Found"));

        List<Reviews> reviews = request.getReviews();

        return Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
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
                .reviews(reviews)
                .rating(calculateAverageRating(reviews))
                .build();
    }

// Calculate overall ratings from reviews
    private double calculateAverageRating(List<Reviews> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (Reviews review : reviews) {
            total += review.getRating();
        }

        return Math.round((total / reviews.size()) * 10.0) / 10.0; // round to 1 decimal
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
                .reviews(product.getReviews())
                .build();
    }
}
