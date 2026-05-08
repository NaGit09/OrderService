package com.furniro.OrderService.utils.error;

import lombok.Getter;

@Getter
public enum CartErrorCode {

    CART_NOT_EXIST(404, "USER CART NOT EXIST"),
    CART_ITEM_NOT_EXIST(404, "CART ITEM NOT EXIST"),

    ;

    private final int code;
    private final String message;

    CartErrorCode(int status, String message) {
        this.code = status;
        this.message = message;
    }

}