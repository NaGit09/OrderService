package com.furniro.OrderService.database.repository.order;

import com.furniro.OrderService.database.entity.order.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Integer> {
    Optional<PromoCode> findByCode(String code);
}
