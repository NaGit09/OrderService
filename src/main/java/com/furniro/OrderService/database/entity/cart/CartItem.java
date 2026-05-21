package com.furniro.OrderService.database.entity.cart;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "CartItem")
@Getter
@Setter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartItemID;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "CartID")
    private Cart cart;

    @Column(name = "VariantID", nullable = false)
    private Integer variantID;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}