package com.example.cartservicesp.cartstore;


import hipstershop.Cart;
import hipstershop.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Lazy
public class RedisCartStore implements ICartStore {

    @Autowired(required = false)
    private RedisTemplate<String, Cart> redisTemplate;

    @Override
    public CompletableFuture<Void> addItemAsync(String userId, String productId, int quantity) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("AddItemAsync called with userId=" + userId + ", productId=" + productId + ", quantity=" + quantity);
            try {
                ValueOperations<String, Cart> ops = redisTemplate.opsForValue();
                Cart cart = ops.get(userId);
                if (cart == null) {
                    cart = Cart.newBuilder().setUserId(userId).build();
                }
                CartItem existingItem = null;
                for (CartItem item : cart.getItemsList()) {
                    if (item.getProductId().equals(productId)) {
                        existingItem = item;
                        break;
                    }
                }
                if (existingItem == null) {
                    cart = cart.toBuilder()
                            .addItems(CartItem.newBuilder()
                                    .setProductId(productId)
                                    .setQuantity(quantity)
                                    .build())
                            .build();
                } else {
                    CartItem updatedItem = existingItem.toBuilder()
                            .setQuantity(existingItem.getQuantity() + quantity)
                            .build();
                    int index = 0;
                    for (int i = 0; i < cart.getItemsCount(); i++) {
                        if (cart.getItems(i).getProductId().equals(productId)) {
                            index = i;
                            break;
                        }
                    }
                    cart = cart.toBuilder()
                            .removeItems(index)
                            .addItems(index, updatedItem)
                            .build();
                }
                ops.set(userId, cart);
                System.out.println("Item added to cart: " + cart);
            } catch (Exception ex) {
                System.out.println("Error adding item to cart: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }


    @Override
    public CompletableFuture<Void> emptyCartAsync(String userId) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("EmptyCartAsync called with userId=" + userId);
            redisTemplate.delete(userId);
        });
    }

    @Override
    public CompletableFuture<Cart> getCartAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("GetCartAsync called with userId=" + userId);
            ValueOperations<String, Cart> ops = redisTemplate.opsForValue();
            Cart cart = ops.get(userId);
            if (cart == null) {
                cart = Cart.newBuilder().setUserId(userId).build();
            }
            return cart;
        });
    }

    @Override
    public boolean ping() {
        try {
            redisTemplate.hasKey("ping");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

