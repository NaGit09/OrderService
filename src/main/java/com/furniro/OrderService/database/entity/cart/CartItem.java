package com.furniro.OrderService.database.entity.cart;

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

    @ManyToOne
    @JoinColumn(name = "CartID")
    private Cart cart;

    @Column(name = "VariantID", nullable = false)
    private Integer variantID;

    @Column(nullable = false)
    private Integer quantity;

    @CreationTimestamp
    private LocalDateTime createdAt;
}