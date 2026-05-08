package com.furniro.OrderService.exception;

import com.furniro.OrderService.utils.error.OrderErrorCode;

import lombok.Getter;

@Getter
public class OrderException extends BaseException {
    private final OrderErrorCode orderErrorCode;

    public OrderException(OrderErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.orderErrorCode = errorCode;
    }
}