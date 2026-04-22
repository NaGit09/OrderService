package com.furniro.OrderService.controller;

import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.req.AddToCartReq;
import com.furniro.OrderService.dto.req.RemoveCartItemReq;
import com.furniro.OrderService.dto.req.UpdateCartReq;
import com.furniro.OrderService.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<AType> addToCart(@RequestBody AddToCartReq req) {
        return cartService.addToCart(req);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<AType> removeCartItem(@RequestBody RemoveCartItemReq req) {
        return cartService.removeCartItem(req);
    }

    @PatchMapping("/update")
    public ResponseEntity<AType> updateCart(@RequestBody UpdateCartReq req) {
        return cartService.updateCart(req);
    }
}
