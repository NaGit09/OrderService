package com.furniro.OrderService.database.entity.order;

import com.furniro.OrderService.utils.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PromoCode")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer promoId;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private BigDecimal minSpend;

    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Integer maxUses;

    @Builder.Default
    @Column(nullable = false)
    private Integer usedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
