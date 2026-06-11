package com.furniro.OrderService.service;

import com.furniro.OrderService.database.entity.order.PromoCode;
import com.furniro.OrderService.database.repository.order.PromoCodeRepository;
import com.furniro.OrderService.exception.OrderException;
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
                .orElseThrow(() -> new OrderException(OrderErrorCode.PROMO_NOT_FOUND));

        // 2. Check active
        if (promo.getActive() == null || !promo.getActive()) {
            throw new OrderException(OrderErrorCode.PROMO_EXPIRED);
        }

        // 3. Check expiration date
        if (promo.getExpiryDate() != null && promo.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new OrderException(OrderErrorCode.PROMO_EXPIRED);
        }

        // 4. Check usage limits
        if (promo.getUsedCount() != null && promo.getMaxUses() != null && 
                promo.getUsedCount() >= promo.getMaxUses()) {
            throw new OrderException(OrderErrorCode.PROMO_LIMIT_REACHED);
        }

        // 5. Check minimum spend
        if (promo.getMinSpend() != null && subtotal.compareTo(promo.getMinSpend()) < 0) {
            throw new OrderException(OrderErrorCode.PROMO_MIN_SPEND_NOT_MET);
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
}
