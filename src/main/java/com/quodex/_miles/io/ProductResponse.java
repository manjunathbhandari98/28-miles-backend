package com.quodex._miles.io;

import com.quodex._miles.entity.ProductFeatures;
import com.quodex._miles.entity.Reviews;
import com.quodex._miles.constant.Gender;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private String productId;
    private String name;
    private String slug;
    private Double price;
    private Double oldPrice;
    private Double tax;
    private Gender gender;
    private List<String> sizes;
    private List<String> colors;
    private List<String> tags;
    private List<String> images;
    private String description;
    private String categoryId;
    private String categoryName;
    private Integer stock;
    private Boolean isTrending;
    private String material;
    private ProductFeatures productFeatures;
    private List<ReviewResponse> reviews;
    private Double rating;
    private LocalDateTime createdAt;
}
