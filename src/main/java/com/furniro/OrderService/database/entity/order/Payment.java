package com.furniro.OrderService.database.entity.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private Integer paymentId;

    @JsonBackReference(value = "order-payments")
    @ManyToOne
    @JoinColumn(name = "OrderID")
    private Order order;

    private String provider;

    private String paypalOrderId;

    private String paypalCaptureId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus= PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentMethod paymentMethod= PaymentMethod.COD;

    private String currency;

    private BigDecimal amount;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
