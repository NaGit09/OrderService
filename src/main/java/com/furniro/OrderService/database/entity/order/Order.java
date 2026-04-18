package com.furniro.OrderService.database.entity.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.furniro.OrderService.utils.enums.OrderStatus;

@Entity
@Table(name = "Orders")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderID;

    @Column(name = "UserID")
    private Integer userID;

    private String address;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    private Integer totalAmount;

    @Builder.Default
    private Integer shippingFee = 0;

    @Column(columnDefinition = "TEXT")
    private String orderNote;

    @Builder.Default
    private LocalDateTime orderedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    // Relationship
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Payment> payments;

}