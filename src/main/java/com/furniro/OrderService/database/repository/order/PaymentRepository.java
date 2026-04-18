package com.furniro.OrderService.database.repository.order;

import com.furniro.OrderService.database.entity.order.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
