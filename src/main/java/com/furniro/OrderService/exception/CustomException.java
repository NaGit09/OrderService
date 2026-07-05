package com.furniro.OrderService.exception;

import com.furniro.OrderService.dto.API.ErrorType;
import com.furniro.OrderService.utils.error.CartErrorCode;
import com.furniro.OrderService.utils.error.OrderErrorCode;

public class CustomException extends RuntimeException {
    
    private ErrorType errorCode;

    public CustomException(ErrorType errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(CartErrorCode error) {
        super(error.getMessage());
        this.errorCode = new ErrorType(error.getCode(), error.getMessage());
    }

    public CustomException(OrderErrorCode error) {
        super(error.getMessage());
        this.errorCode = new ErrorType(error.getCode(), error.getMessage());
    }

    public ErrorType getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorType errorCode) {
        this.errorCode = errorCode;
    }
}