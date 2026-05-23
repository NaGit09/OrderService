package com.furniro.OrderService.dto.res;

import com.furniro.OrderService.database.entity.order.Order;
import com.furniro.OrderService.database.entity.order.OrderItem;
import com.furniro.OrderService.utils.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryRes {
    private Integer orderID;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime orderedAt;
    private int totalItems;

    public static OrderHistoryRes fromEntity(Order order) {
        int totalQty = 0;
        if (order.getItems() != null) {
            totalQty = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        }

        return OrderHistoryRes.builder()
                .orderID(order.getOrderID())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .orderedAt(order.getOrderedAt())
                .totalItems(totalQty)
                .build();
    }
}
