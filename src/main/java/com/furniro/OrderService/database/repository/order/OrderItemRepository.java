package com.furniro.OrderService.database.repository.order;

import com.furniro.OrderService.database.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furniro.OrderService.utils.enums.OrderStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.order.userID = :userID " +
           "AND oi.order.status = :status AND oi.variant IN :variantIDs")
    boolean existsByUserIDAndStatusAndVariantIn(
            @Param("userID") Integer userID,
            @Param("status") OrderStatus status,
            @Param("variantIDs") List<Integer> variantIDs);
}
