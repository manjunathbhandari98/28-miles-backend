package com.quodex._miles.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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


    @PrePersist
    public void generateCartId(){
        if(this.cartId == null){
            this.cartId = "CART"+ UUID.randomUUID().toString().toUpperCase().substring(0,7);
        }
    }
}
