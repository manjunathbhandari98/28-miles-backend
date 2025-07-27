package com.quodex._miles.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String productId;
    private String productName;
    private String size;
    private String color;
    private int quantity;
    private double price;
    private String image;

    @PrePersist
    public void generateOrderItemId(){
        if(this.orderItemId == null){
            this.orderItemId = "ORDITM"+ UUID.randomUUID().toString().toUpperCase().substring(0,7);
        }
    }
}
