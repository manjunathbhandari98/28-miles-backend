package com.quodex._miles.entity;

import com.quodex._miles.constant.OrderStatus;
import com.quodex._miles.constant.PaymentMethod;
import com.quodex._miles.io.PaymentDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @ManyToOne
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;

    private double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // e.g., PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, PAYMENT_FAILED

    private LocalDateTime orderDate;


    // Properly embed PaymentDetails
    @Embedded
    private PaymentDetails paymentDetails;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Double deliveryCharges;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime estimatedDelivery;

    @PrePersist
    public void generateOrderId(){
        if(this.orderId == null){
            this.orderId = "ORD"+ UUID.randomUUID().toString().toUpperCase().substring(0,7);
        }
        if(this.orderDate == null){
            this.orderDate = LocalDateTime.now();
        }
        if(this.status == null){
            this.status = OrderStatus.PENDING;
        }
    }
}
