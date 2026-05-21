package com.furniro.OrderService.dto.req;

import com.furniro.OrderService.utils.enums.CartAction;

import lombok.Data;

@Data
public class UpdateCartReq {
    private Integer cartID;
    
    private Integer userID;

    private Integer variantID;

    private Integer quantity;

    private CartAction action;

    private Double price;
}