package com.furniro.OrderService.utils.enums;

public enum OrderStatus {
    PENDING, // mới tạo
    CREATED, // đã tạo PayPal order
    APPROVED, // user đã approve
    COMPLETED, // đã capture tiền
    FAILED, // thất bại
    CANCELLED // hủy
}