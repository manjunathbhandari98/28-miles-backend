package com.quodex._miles.service.Impl;

import com.quodex._miles.constant.Gender;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final FileUploadServiceImpl fileUploadService;

    @Override
    public ProductResponse addProduct(ProductRequest request,
             List<MultipartFile> files
    )
    {
        String slug = (request.getSlug() == null || request.getSlug().isEmpty())
                ? SlugUtil.toSlug(request.getName())
                : SlugUtil.toSlug(request.getSlug());

        if (productRepository.existsBySlug(slug)) {
            throw new AlreadyExistsException("Product Already Exists");
        }

        Category category = categoryRepository.findByCategoryId(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category Not Found"));

        if (files != null && !files.isEmpty()) {
            List<String> imgUrls = files.stream()
                    .map(fileUploadService::uploadFile)
                    .collect(Collectors.toList());

            request.setImages(imgUrls);
        }

        double taxApplicable = request.getPrice() >= 1000
                ? (request.getPrice() * 0.12)
                : (request.getPrice() * 0.05);



        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .category(category)
                .colors(request.getColors())
                .sizes(request.getSizes())
                .tags(request.getTags())
                .images(request.getImages())
                .description(request.getDescription())
                .summary(request.getSummary())
                .price(request.getPrice())
                .oldPrice(request.getOldPrice())
                .tax(taxApplicable)
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
    public Page<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending()); // optional sorting
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(this::convertToResponse);
    }

    @Override
    public Page<ProductResponse> getNewArrivals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 30 days ago
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);

        Page<Product> productPage = productRepository.findNewArrivals(startDate, pageable);

        // Convert to DTO
        return productPage.map(this::convertToResponse);
    }


    @Override
    public Page<ProductResponse> getProductsByFilters(Gender gender, String  categorySlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Product> productPage;

        if (gender != null && categorySlug != null) {
            productPage = productRepository.findByGenderAndCategory_Slug(gender, categorySlug, pageable);
        } else if (gender != null) {
            productPage = productRepository.findByGender(gender, pageable);
        } else if (categorySlug != null) {
            productPage = productRepository.findByCategory_Slug(categorySlug, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(this::convertToResponse);
    }

    @Override
    public Page<ProductResponse> getSimilarProducts(String categoryId, int page, int size){
        Pageable pageable = PageRequest.of(page,size);
        Page<Product> productPage;
        if(categoryId != null){
            productPage = productRepository.findByCategory_CategoryId(categoryId,pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }
        return productPage.map(this::convertToResponse);
    }


    @Override
    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
        return convertToResponse(product);
    }

    @Override
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
        return convertToResponse(product);
    }


    @Override
    public ProductResponse updateProduct(String productId, ProductRequest request, List<MultipartFile> files) {
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

        //  Combine old images + new images
        List<String> combinedImages = new ArrayList<>();

        if (request.getImages() != null) {
            combinedImages.addAll(request.getImages()); // Keep selected old images
        }

        if (files != null && !files.isEmpty()) {
            List<String> newUploadedImages = files.stream()
                    .map(fileUploadService::uploadFile)
                    .toList();
            combinedImages.addAll(newUploadedImages);
        }

        double taxApplicable = request.getPrice() >= 1000
                ? (request.getPrice() * 0.12)
                : (request.getPrice() * 0.05);


        product.setName(request.getName());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setSummary(request.getSummary());
        product.setPrice(request.getPrice());
        product.setOldPrice(request.getOldPrice());
        product.setTax(taxApplicable);
        product.setColors(request.getColors());
        product.setSizes(request.getSizes());
        product.setTags(request.getTags());
        product.setImages(combinedImages); // final image list (old + new)
        product.setProductFeatures(request.getProductFeatures());
        product.setMaterial(request.getMaterial());
        product.setGender(request.getGender());
        product.setStock(request.getStock());
        product.setIsTrending(request.getIsTrending());
        product.setCategory(category);

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
                .summary(product.getSummary())
                .price(product.getPrice())
                .oldPrice(product.getOldPrice())
                .tax(product.getTax())
                .rating(product.getRating())
                .gender(product.getGender())
                .material(product.getMaterial())
                .stock(product.getStock())
                .isTrending(product.getIsTrending())
                .sizes(product.getSizes())
                .colors(product.getColors())
                .tags(product.getTags())
                .images(product.getImages())
                .categoryName(product.getCategory().getCategoryName())
                .categoryId(product.getCategory().getCategoryId())
                .productFeatures(product.getProductFeatures())
                .reviews(mapReviews(reviewRepository.findByProduct(product)))
                .createdAt(product.getCreatedAt())
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
                        .username(review.getUser().getName())
                        .productId(review.getProduct().getProductId())
                        .images(review.getImages())
                        .createdAt(review.getCreatedAt())
                        .build())
                .toList();
    }

}

