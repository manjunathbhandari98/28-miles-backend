package com.quodex._miles.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String addressId;

    private String fullName;

    private String phone;

    private String email;

    private String street;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    private boolean isDefault;

    // Optional relationship to User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void prePersist(){
        if (this.addressId == null || this.addressId.isEmpty()){
            this.addressId = "ADR"+UUID.randomUUID().toString().toUpperCase().substring(0,7);
        }

    }
}
