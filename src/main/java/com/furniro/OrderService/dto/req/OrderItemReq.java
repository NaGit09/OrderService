package com.furniro.OrderService.dto.req;

import lombok.Data;

@Data
public class OrderItemReq {
    private Integer productVariantID;
    private Integer quantity;
    private Integer price;
}