package com.quodex._miles.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cart")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String cartId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;
    private LocalDateTime exceptedDate;
    private Double subTotal;
    private Double totalDiscount;
    private Double totalTax;
    private Double grandTotal;


    @PrePersist
    public void prePersist(){
        if(this.cartId == null){
            this.cartId = "CART"+ UUID.randomUUID().toString().toUpperCase().substring(0,7);
        }
        if (this.createdAt == null){
            this.createdAt = LocalDateTime.now();
        }
    }
}
