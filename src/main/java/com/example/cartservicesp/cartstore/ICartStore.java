package com.example.cartservicesp.cartstore;

import hipstershop.Cart;
import hipstershop.CartItem;
import java.util.concurrent.CompletableFuture;

public interface ICartStore {
    CompletableFuture<Void> addItemAsync(String userId, String productId, int quantity);
    CompletableFuture<Void> emptyCartAsync(String userId);
    CompletableFuture<Cart> getCartAsync(String userId);
    boolean ping();
}

