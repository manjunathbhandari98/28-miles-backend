package com.quodex._miles.io;

import com.quodex._miles.entity.ProductFeatures;
import com.quodex._miles.entity.Reviews;
import com.quodex._miles.constant.Gender;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    private String name;
    private String slug;
    private Double price;
    private Double oldPrice;
    private Gender gender;
    private List<String> sizes;
    private List<String> colors;
    private List<String> tags;
    private List<String> images;
    private String description;
    private String categoryId; // You send this and fetch Category entity from DB
    private Integer stock;
    private Boolean isTrending;
    private String material;
    private ProductFeatures productFeatures;
}
