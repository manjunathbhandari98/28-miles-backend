package com.quodex._miles.entity;

import com.quodex._miles.constant.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private Double price;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Double oldPrice;
    private Double tax;

    // Assuming Sizes is an @Embeddable or @ElementCollection
    @ElementCollection
    private List<String> sizes;

    @ElementCollection
    private List<String> colors;

    private Double rating;

    @ElementCollection
    private List<String> tags;

    @ElementCollection
    private List<String> images;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    private Integer stock;

    private Boolean isTrending;

    private String material;

    @Embedded
    private ProductFeatures productFeatures;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reviews> reviews;

    private LocalDateTime createdAt;


    @PrePersist
    public void prePersist() {
        if (this.productId == null) {
            this.productId = "PRD" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);
        }
        if (this.createdAt == null){
            this.createdAt = LocalDateTime.now();
        }
    }
}
