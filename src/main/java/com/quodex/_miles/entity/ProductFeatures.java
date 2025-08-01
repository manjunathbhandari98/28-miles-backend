package com.quodex._miles.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFeatures {
    private String design;
    private String occasion;
    private String fabric;
    private String fit;
    private String sleeve;
    private String neck;
    private String pockets;
    private String waist;
    private String length;
}
