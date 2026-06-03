package com.furniro.OrderService.utils.error;

import lombok.Getter;

@Getter
public enum OrderErrorCode {

    ORDER_NOT_EXIST(404, "ORDER NOT EXIST"),
    ORDER_ITEM_NOT_EXIST(404, "ORDER ITEM NOT EXIST"),
    PROMO_NOT_FOUND(404, "PROMO CODE NOT FOUND"),
    PROMO_EXPIRED(400, "PROMO CODE HAS EXPIRED"),
    PROMO_LIMIT_REACHED(400, "PROMO CODE USAGE LIMIT REACHED"),
    PROMO_MIN_SPEND_NOT_MET(400, "ORDER MINIMUM SPEND NOT MET FOR PROMO"),
    PRICE_MISMATCH(400, "BACKEND CATALOG PRICE MISMATCH"),

    ;

    private final int code;
    private final String message;

    OrderErrorCode(int status, String message) {
        this.code = status;
        this.message = message;
    }

}