package com.furniro.OrderService.database.entity.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.furniro.OrderService.utils.enums.PaymentMethod;
import com.furniro.OrderService.utils.enums.PaymentStatus;

@Entity
@Table(name = "Payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentID;

    @ManyToOne
    @JoinColumn(name = "OrderID")
    private Order order;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String transactionID;

    private Integer amount;

    @Builder.Default
    private String currency = "VND";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String providerResponse = "";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime paidAt;

}
