package com.furniro.OrderService.database.repository.order;

import com.furniro.OrderService.database.entity.order.Order;
import com.furniro.OrderService.utils.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    List<Order> findByUserID(Integer userID);

    Page<Order> findByUserID(Integer userID, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByStatusAndUserID(OrderStatus status, Integer userID, Pageable pageable);

    Page<Order> findAll(Pageable pageable);

    Page<Order> findByUserIDAndStatus(Integer userID, OrderStatus status, Pageable pageable);
}
