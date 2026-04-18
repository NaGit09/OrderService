package com.furniro.OrderService.utils;

import com.furniro.OrderService.utils.enums.CartAction;

public class CartUtil {

    public static int calculateQuantity(int current, int value, CartAction action) {

        switch (action) {

            case ADD:
                return current + value;

            case SUBTRACT:
                return Math.max(0, current - value);

            default:
                throw new IllegalArgumentException("Invalid action");
        }
    }
}