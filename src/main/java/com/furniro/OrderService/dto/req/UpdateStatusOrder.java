package com.furniro.OrderService.dto.req;

import com.furniro.OrderService.utils.enums.OrderStatus;
import lombok.Data;

@Data
public class UpdateStatusOrder {
    private Integer orderID;
    private OrderStatus status;
}