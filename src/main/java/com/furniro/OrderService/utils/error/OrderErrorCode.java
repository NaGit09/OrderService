package com.furniro.OrderService.utils.error;

import lombok.Getter;

@Getter
public enum OrderErrorCode {

    ORDER_NOT_EXIST(404, "ORDER NOT EXIST"),
    ORDER_ITEM_NOT_EXIST(404, "ORDER ITEM NOT EXIST"),

    ;

    private final int code;
    private final String message;

    OrderErrorCode(int status, String message) {
        this.code = status;
        this.message = message;
    }

}