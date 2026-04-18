package com.furniro.OrderService.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartReq {

    @NotNull(message = "Cart ID is required")
    private int cartID;

    @NotNull(message = "User ID is required")
    private int userID;

    @NotNull(message = "Variant ID is required")
    private int variantID;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @NotNull(message = "Quantity is required")
    private int quantity;
}