package com.quodex._miles.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String categoryId;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false, unique = true)
    private String slug;

    @PrePersist
    public void generateCategoryId() {
        if (this.categoryId == null) {
            this.categoryId = "CAT" + UUID.randomUUID().toString().toUpperCase().substring(0, 7);
        }
    }
}

