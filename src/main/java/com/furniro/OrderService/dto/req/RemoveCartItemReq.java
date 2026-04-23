package com.furniro.OrderService.dto.req;

import lombok.Data;

@Data
public class RemoveCartItemReq {
    private int cartID;
    private int userID;
    private int variantID;
}