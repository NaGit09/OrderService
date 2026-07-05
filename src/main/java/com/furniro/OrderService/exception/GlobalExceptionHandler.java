package com.furniro.OrderService.exception;

import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.API.ErrorType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<AType> handleAppExceptions(CustomException ex) {
        ErrorType error = ex.getErrorCode();

        return new ResponseEntity<>(error, HttpStatus.valueOf(error.getCode()));
    }
}
