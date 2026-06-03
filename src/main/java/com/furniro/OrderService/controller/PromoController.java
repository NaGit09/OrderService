package com.furniro.OrderService.controller;

import com.furniro.OrderService.database.entity.order.PromoCode;
import com.furniro.OrderService.database.repository.order.PromoCodeRepository;
import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.API.ApiType;
import com.furniro.OrderService.service.PromoCodeService;
import com.furniro.OrderService.utils.enums.DiscountType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders/promos")
@RequiredArgsConstructor
@Slf4j
public class PromoController {

    private final PromoCodeService promoCodeService;
    private final PromoCodeRepository promoCodeRepository;

    /**
     * Seed sample promo codes in the database for testing and demonstration.
     */
    @PostMapping("/seed")
    public ResponseEntity<AType> seedPromoCodes() {
        log.info("Seeding database with default promotional codes...");
        
        // Remove existing to avoid key conflict on duplicate seed
        promoCodeRepository.findByCode("WELCOME10").ifPresent(p -> promoCodeRepository.delete(p));
        promoCodeRepository.findByCode("FURNIRO50").ifPresent(p -> promoCodeRepository.delete(p));
        promoCodeRepository.findByCode("EXPIRED20").ifPresent(p -> promoCodeRepository.delete(p));

        PromoCode welcome = PromoCode.builder()
                .code("WELCOME10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00")) // 10% Off
                .minSpend(BigDecimal.ZERO)
                .maxUses(1000)
                .usedCount(0)
                .active(true)
                .build();

        PromoCode furniro50 = PromoCode.builder()
                .code("FURNIRO50")
                .discountType(DiscountType.FLAT)
                .discountValue(new BigDecimal("50.00")) // $50.00 Off
                .minSpend(new BigDecimal("200.00")) // Minimum spend of $200.00
                .maxUses(500)
                .usedCount(0)
                .active(true)
                .build();

        PromoCode expired = PromoCode.builder()
                .code("EXPIRED20")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20.00")) // 20% Off
                .minSpend(BigDecimal.ZERO)
                .maxUses(10)
                .usedCount(0)
                .expiryDate(LocalDateTime.now().minusHours(1)) // Expired 1 hour ago
                .active(true)
                .build();

        promoCodeRepository.saveAll(List.of(welcome, furniro50, expired));
        log.info("Successfully seeded promo codes: WELCOME10, FURNIRO50, EXPIRED20");

        return ResponseEntity.ok(ApiType.success("Promo codes seeded successfully. Try checkout with WELCOME10 (10% off) or FURNIRO50 ($50 off with min spend $200)."));
    }

    /**
     * Validate a promo code and get calculated discount details prior to checkout.
     */
    @GetMapping("/validate")
    public ResponseEntity<AType> validatePromoCode(
            @RequestParam String code,
            @RequestParam BigDecimal subtotal) {
        
        log.info("Validating promo code: {} for subtotal: {}", code, subtotal);
        PromoCode promo = promoCodeService.validateAndCalculateDiscount(code, subtotal);
        BigDecimal discountAmount = promoCodeService.calculateDiscountAmount(promo, subtotal);

        Map<String, Object> result = Map.of(
                "code", promo.getCode(),
                "discountType", promo.getDiscountType(),
                "discountValue", promo.getDiscountValue(),
                "subtotal", subtotal,
                "discountAmount", discountAmount,
                "finalAmount", subtotal.subtract(discountAmount)
        );

        return ResponseEntity.ok(ApiType.success(result));
    }
}
