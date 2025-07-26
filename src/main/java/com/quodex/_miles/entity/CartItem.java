package com.quodex._miles.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String cartItemId;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    private String productId;
    private String productName;
    private String size;
    private String color;
    private int quantity;
    private double price;
    private String image;

    @PrePersist
    public void generateCartId(){
        if(this.cartItemId == null){
            this.cartItemId = "CRT-ITM-"+ UUID.randomUUID().toString().toUpperCase().substring(0,7);
        }
    }
}
