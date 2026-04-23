package com.furniro.OrderService.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.furniro.OrderService.database.entity.cart.Cart;
import com.furniro.OrderService.database.entity.cart.CartItem;
import com.furniro.OrderService.database.repository.cart.CartItemRepository;
import com.furniro.OrderService.database.repository.cart.CartRepository;
import com.furniro.OrderService.dto.API.AType;
import com.furniro.OrderService.dto.API.ApiType;
import com.furniro.OrderService.dto.req.AddToCartReq;
import com.furniro.OrderService.dto.req.RemoveCartItemReq;
import com.furniro.OrderService.dto.req.UpdateCartReq;
import com.furniro.OrderService.exception.CartException;
import com.furniro.OrderService.utils.CartUtil;
import com.furniro.OrderService.utils.enums.CartErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public ResponseEntity<AType> addToCart(AddToCartReq req) {

        Cart cart = cartRepository.findByCartIDAndUserID(req.getCartID(), req.getUserID())
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_EXIST));

        Optional<CartItem> existingOpt = cartItemRepository
                .findByCartAndVariantID(cart, req.getVariantID());

        CartItem cartItem;
        
        if (existingOpt.isPresent()) {

            cartItem = existingOpt.get();

            Integer newQuantity = cartItem.getQuantity() + req.getQuantity();

            cartItem.setQuantity(newQuantity);
            
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setVariantID(req.getVariantID());
            cartItem.setQuantity(req.getQuantity());
        }

        cartItemRepository.save(cartItem);

        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Add to cart successfully")
                .data(true)
                .build());
    }

    @Transactional
    public ResponseEntity<AType> removeCartItem(RemoveCartItemReq req) {

        Cart cart = cartRepository.findByCartIDAndUserID(req.getCartID(), req.getUserID())
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_EXIST));

        CartItem cartItem = cartItemRepository.findByCartAndVariantID(cart, req.getVariantID())
                .orElseThrow(() -> new CartException(CartErrorCode.CART_ITEM_NOT_EXIST));

        cartItemRepository.delete(cartItem);

        return ResponseEntity
                .ok(ApiType.builder()
                        .code(200)
                        .message("Remove cart item successfully")
                        .data(true)
                        .build());
    }

    @Transactional
    public ResponseEntity<AType> updateCart(UpdateCartReq req) {

        Cart cart = cartRepository.findByCartIDAndUserID(req.getCartID(), req.getUserID())
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_EXIST));

        CartItem cartItem = cartItemRepository.findByCartAndVariantID(cart, req.getVariantID())
                .orElseThrow(() -> new CartException(CartErrorCode.CART_ITEM_NOT_EXIST));

        int newQuantity = CartUtil.calculateQuantity(
            cartItem.getQuantity(),
            req.getQuantity(),
            req.getAction()
        );

        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }

        return ResponseEntity.ok(ApiType.builder()
                .code(200)
                .message("Update cart successfully")
                .data(true)
                .build());
    }

}