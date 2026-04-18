package com.furniro.OrderService.database.repository.cart;

import com.furniro.OrderService.database.entity.cart.Cart;
import com.furniro.OrderService.database.entity.cart.CartItem;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
        Optional<CartItem> findByCartAndVariantID(Cart cart, Integer variantID);

}
