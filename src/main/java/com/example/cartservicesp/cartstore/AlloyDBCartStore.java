package com.example.cartservicesp.cartstore;

import hipstershop.Cart;
import hipstershop.CartItem;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

@Service
@Lazy
public class AlloyDBCartStore implements ICartStore {

    private final String url;
    private final String user;
    private final String password;

    public AlloyDBCartStore() {
        this.url = System.getenv("ALLOY_DB_URL");
        this.user = System.getenv("ALLOY_DB_USER");
        this.password = System.getenv("ALLOY_DB_PASSWORD");
    }

    @Override
    public CompletableFuture<Void> addItemAsync(String userId, String productId, int quantity) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                String selectSQL = "SELECT quantity FROM CartItems WHERE userId=? AND productId=?";
                try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
                    selectStmt.setString(1, userId);
                    selectStmt.setString(2, productId);
                    ResultSet resultSet = selectStmt.executeQuery();
                    int currentQuantity = 0;
                    if (resultSet.next()) {
                        currentQuantity = resultSet.getInt("quantity");
                    }
                    int totalQuantity = quantity + currentQuantity;

                    String insertSQL = "INSERT INTO CartItems (userId, productId, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                        insertStmt.setString(1, userId);
                        insertStmt.setString(2, productId);
                        insertStmt.setInt(3, totalQuantity);
                        insertStmt.executeUpdate();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> emptyCartAsync(String userId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                String deleteSQL = "DELETE FROM CartItems WHERE userId=?";
                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSQL)) {
                    deleteStmt.setString(1, userId);
                    deleteStmt.executeUpdate();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Cart> getCartAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            Cart cart = Cart.newBuilder()
                            .setUserId(userId).build();
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                String selectSQL = "SELECT productId, quantity FROM CartItems WHERE userId=?";
                try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
                    selectStmt.setString(1, userId);
                    ResultSet resultSet = selectStmt.executeQuery();
                    while (resultSet.next()) {
                        CartItem item = CartItem.newBuilder()
                                                .setProductId(resultSet.getString("productId"))
                                                .setQuantity(resultSet.getInt("quantity")).build();


                        cart = cart.toBuilder().addItems(item).build();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return cart;
        });
    }

    @Override
    public boolean ping() {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

