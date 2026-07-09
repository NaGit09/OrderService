package com.furniro.OrderService.controller;

import com.furniro.OrderService.database.entity.order.PromoCode;
import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.API.ApiType;
import com.furniro.OrderService.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/orders/promos")
@RequiredArgsConstructor
@Slf4j
public class PromoController {

    private final PromoCodeService promoCodeService;

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

