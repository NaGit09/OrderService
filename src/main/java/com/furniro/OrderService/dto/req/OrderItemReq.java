package com.furniro.OrderService.dto.req;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemReq {
    private Integer variantID;
    private Integer quantity;
    private BigDecimal price;
}