package com.furniro.OrderService.exception;

import com.furniro.OrderService.utils.error.CartErrorCode;

import lombok.Getter;

@Getter
public class CartException extends BaseException {

    private final CartErrorCode cartErrorCode;

    public CartException(CartErrorCode cartErrorCode) {
        super(cartErrorCode.getCode(), cartErrorCode.getMessage());
        this.cartErrorCode = cartErrorCode;
    }
}