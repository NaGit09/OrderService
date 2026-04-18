package com.furniro.OrderService.dto.req;

import com.furniro.OrderService.utils.enums.CartAction;

import lombok.Data;

@Data
public class UpdateCartReq {
    private int cartID;
    private int userID;
    private int variantID;
    private int quantity;
    private CartAction action;
}