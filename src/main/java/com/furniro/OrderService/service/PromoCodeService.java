package com.furniro.OrderService.service;

import com.furniro.OrderService.database.entity.order.PromoCode;
import com.furniro.OrderService.database.repository.order.PromoCodeRepository;
import com.furniro.OrderService.exception.CustomException;
import com.furniro.OrderService.utils.enums.DiscountType;
import com.furniro.OrderService.utils.error.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    /**
     * Validates a promotional code against a given subtotal and calculates the discount amount.
     */
    public PromoCode validateAndCalculateDiscount(String codeStr, BigDecimal subtotal) {
        if (codeStr == null || codeStr.trim().isEmpty()) {
            return null;
        }

        // 1. Fetch promo code
        PromoCode promo = promoCodeRepository.findByCode(codeStr.trim().toUpperCase())
                .orElseThrow(() -> new CustomException(OrderErrorCode.PROMO_NOT_FOUND));

        // 2. Check active
        if (promo.getActive() == null || !promo.getActive()) {
            throw new CustomException(OrderErrorCode.PROMO_EXPIRED);
        }

        // 3. Check expiration date
        if (promo.getExpiryDate() != null && promo.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CustomException(OrderErrorCode.PROMO_EXPIRED);
        }

        // 4. Check usage limits
        if (promo.getUsedCount() != null && promo.getMaxUses() != null && 
                promo.getUsedCount() >= promo.getMaxUses()) {
            throw new CustomException(OrderErrorCode.PROMO_LIMIT_REACHED);
        }

        // 5. Check minimum spend
        if (promo.getMinSpend() != null && subtotal.compareTo(promo.getMinSpend()) < 0) {
            throw new CustomException(OrderErrorCode.PROMO_MIN_SPEND_NOT_MET);
        }

        return promo;
    }

    /**
     * Compute discount value.
     */
    public BigDecimal calculateDiscountAmount(PromoCode promo, BigDecimal subtotal) {
        if (promo == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            // discount = subtotal * discountValue / 100
            discount = subtotal.multiply(promo.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else if (promo.getDiscountType() == DiscountType.FLAT) {
            discount = promo.getDiscountValue();
        }

        // Ensure discount does not exceed the subtotal
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return discount;
    }

    /**
     * Transactionally increment the usage count of the promo code.
     */
    @Transactional
    @CacheEvict(value = "order:promo", key = "#promo.code.toUpperCase()")
    public void consumePromo(PromoCode promo) {
        if (promo == null) {
            return;
        }
        
        log.info("Consuming promo code: {}, current usage count: {}", promo.getCode(), promo.getUsedCount());
        promo.setUsedCount(promo.getUsedCount() + 1);
        if (promo.getUsedCount() >= promo.getMaxUses()) {
            promo.setActive(false);
            log.info("Promo code {} has reached maximum usage count ({}) and is now inactive.", promo.getCode(), promo.getMaxUses());
        }
        promoCodeRepository.save(promo);
    }

    /**
     * Synchronize a promo code event from Kafka.
     */
    @Transactional
    @CacheEvict(value = "order:promo", key = "#event.get('code').toString().toUpperCase()", condition = "#event.get('code') != null")
    public void syncPromoCode(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        String rawCode = (String) event.get("code");
        if (rawCode == null) return;
        final String code = rawCode.trim().toUpperCase();

        if ("DELETED".equals(eventType)) {
            promoCodeRepository.findByCode(code).ifPresent(promo -> {
                promoCodeRepository.delete(promo);
                log.info("Deleted PromoCode: {}", code);
            });
            return;
        }

        // Parse discountType
        String typeStr = (String) event.get("discountType");
        DiscountType discountType = DiscountType.PERCENTAGE;
        if (typeStr != null) {
            String normalizedType = typeStr.toUpperCase();
            if ("PERCENT".equals(normalizedType)) {
                normalizedType = "PERCENTAGE";
            }
            try {
                discountType = DiscountType.valueOf(normalizedType);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid discountType '{}', defaulting to PERCENTAGE", typeStr);
            }
        }

        // Parse discountValue
        BigDecimal discountValue = BigDecimal.ZERO;
        Object valObj = event.get("discountValue");
        if (valObj != null) {
            discountValue = new BigDecimal(valObj.toString());
        }

        // Parse minSpend
        BigDecimal minSpend = BigDecimal.ZERO;
        Object minSpendObj = event.get("minSpend");
        if (minSpendObj != null) {
            minSpend = new BigDecimal(minSpendObj.toString());
        }

        // Parse maxUses
        Integer maxUses = 1000;
        Object maxUsesObj = event.get("maxUses");
        if (maxUsesObj != null) {
            maxUses = ((Number) maxUsesObj).intValue();
        }

        // Parse expiryDate
        LocalDateTime expiryDate = null;
        String expiryStr = (String) event.get("expiryDate");
        if (expiryStr != null && !expiryStr.trim().isEmpty()) {
            try {
                expiryDate = LocalDateTime.parse(expiryStr);
            } catch (Exception e) {
                log.warn("Failed to parse expiryDate '{}'", expiryStr);
            }
        }

        // Parse active/status
        Boolean active = true;
        Object statusObj = event.get("status");
        if (statusObj != null) {
            active = (Boolean) statusObj;
        }

        if ("CREATED".equals(eventType)) {
            if (promoCodeRepository.findByCode(code).isPresent()) {
                log.info("PromoCode {} already exists, treating as update", code);
                updateExistingPromo(code, discountType, discountValue, minSpend, expiryDate, maxUses, active);
            } else {
                PromoCode promoCode = PromoCode.builder()
                        .code(code)
                        .discountType(discountType)
                        .discountValue(discountValue)
                        .minSpend(minSpend)
                        .expiryDate(expiryDate)
                        .maxUses(maxUses)
                        .usedCount(0)
                        .active(active)
                        .build();
                promoCodeRepository.save(promoCode);
                log.info("Successfully created synced PromoCode: {}", code);
            }
        } else if ("UPDATED".equals(eventType)) {
            updateExistingPromo(code, discountType, discountValue, minSpend, expiryDate, maxUses, active);
        }
    }

    private void updateExistingPromo(String code, DiscountType discountType, BigDecimal discountValue,
                                     BigDecimal minSpend, LocalDateTime expiryDate, Integer maxUses, Boolean active) {
        PromoCode promo = promoCodeRepository.findByCode(code)
                .orElseGet(() -> PromoCode.builder().code(code).usedCount(0).build());
        promo.setDiscountType(discountType);
        promo.setDiscountValue(discountValue);
        promo.setMinSpend(minSpend);
        promo.setExpiryDate(expiryDate);
        promo.setMaxUses(maxUses);
        promo.setActive(active);
        promoCodeRepository.save(promo);
        log.info("Successfully updated synced PromoCode: {}", code);
    }
}
