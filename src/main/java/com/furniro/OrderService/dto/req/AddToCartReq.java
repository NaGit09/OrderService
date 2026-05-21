package com.furniro.OrderService.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartReq {

    @NotNull(message = "Cart ID is required")
    private Integer cartID;

    @NotNull(message = "User ID is required")
    private Integer userID;

    @NotNull(message = "Variant ID is required")
    private Integer variantID;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Price is required")
    private Double price;

}