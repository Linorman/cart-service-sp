package com.example.cartservicesp.cartstore;

import hipstershop.Cart;
import hipstershop.CartItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class InMemoryCartStore implements ICartStore {

    private final Map<String, Cart> cartData = new HashMap<>();

    @Override
    public CompletableFuture<Void> addItemAsync(String userId, String productId, int quantity) {
        return CompletableFuture.runAsync(() -> {
            Cart cart = cartData.getOrDefault(userId, Cart.newBuilder().setUserId(userId).build());
            List<CartItem> items = new ArrayList<>(cart.getItemsList());
            boolean found = false;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getProductId().equals(productId)) {
                    items.set(i, items.get(i).toBuilder().setQuantity(items.get(i).getQuantity() + quantity).build());
                    found = true;
                    break;
                }
            }
            if (!found) {
                items.add(CartItem.newBuilder().setProductId(productId).setQuantity(quantity).build());
            }
            cart = cart.toBuilder().clearItems().addAllItems(items).build();
            cartData.put(userId, cart);
        });
    }

    @Override
    public CompletableFuture<Void> emptyCartAsync(String userId) {
        return CompletableFuture.runAsync(() -> cartData.remove(userId));
    }

    @Override
    public CompletableFuture<Cart> getCartAsync(String userId) {
        return CompletableFuture.completedFuture(cartData.getOrDefault(userId, Cart.newBuilder().setUserId(userId).build()));
    }

    @Override
    public boolean ping() {
        return true;
    }
}