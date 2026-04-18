package com.furniro.OrderService.exception;

import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.API.ErrorType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<AType> handleAppExceptions(BaseException ex) {
        AType error = ErrorType.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getCode()));
    }
}
