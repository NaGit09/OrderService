package com.furniro.OrderService.database.entity.order;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "OrderItem")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderItemID;

    @ManyToOne
    @JoinColumn(name = "OrderID")
    private Order order;

    @Column(name = "VariantID", nullable = false)
    private Integer variant;

    private Integer quantity;

    // Price after sale
    private BigDecimal priceAtPurchase;
}
