package com.furniro.OrderService.dto.req;


import lombok.Data;

import java.util.List;

import com.furniro.OrderService.utils.enums.OrderStatus;
import com.furniro.OrderService.utils.enums.PaymentMethod;

@Data
public class CreateOrderReq {

    private Integer userID;

    private String note;

    private int shippingFee;

    private OrderStatus orderStatus;
    
    private String address;

    private PaymentMethod paymentMethod;
    
    private String currency;

    private List<OrderItemReq> orderItems;

    private String returnUrl;

    private String cancelUrl;

    private String promoCode;

}