package com.example.cartservicesp.cartstore;

import hipstershop.Cart;
import hipstershop.CartItem;
import com.google.cloud.spanner.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Service
@Lazy
public class SpannerCartStore implements ICartStore {

    private final String projectId;

    private final String instanceId;

    private final String databaseId;

    public SpannerCartStore() {
        this.projectId = System.getenv("GCP_PROJECT_ID");
        this.instanceId = System.getenv("SPANNER_INSTANCE_ID");
        this.databaseId = System.getenv("SPANNER_DATABASE_ID");
    }

    private Spanner getSpanner() {
        SpannerOptions options = SpannerOptions.newBuilder().setProjectId(projectId).build();
        return options.getService();
    }

    private DatabaseClient getDatabaseClient() {
        Spanner spanner = getSpanner();
        DatabaseId db = DatabaseId.of(projectId, instanceId, databaseId);
        return spanner.getDatabaseClient(db);
    }

    @Override
    public CompletableFuture<Void> addItemAsync(String userId, String productId, int quantity) {
        return CompletableFuture.runAsync(() -> {
            DatabaseClient dbClient = getDatabaseClient();
            dbClient.readWriteTransaction().run(transaction -> {
                Struct row = transaction.readRow("CartItems", Key.of(userId, productId), Collections.singleton("quantity"));
                long currentQuantity = 0;
                if (row != null) {
                    currentQuantity = row.isNull("quantity") ? 0 : row.getLong("quantity");
                }
                int totalQuantity = (int) (quantity + currentQuantity);

                Mutation mutation = Mutation.newInsertOrUpdateBuilder("CartItems")
                        .set("userId").to(userId)
                        .set("productId").to(productId)
                        .set("quantity").to(totalQuantity)
                        .build();
                transaction.buffer(mutation);
                return null;
            });
        });
    }

    @Override
    public CompletableFuture<Void> emptyCartAsync(String userId) {
        return CompletableFuture.runAsync(() -> {
            DatabaseClient dbClient = getDatabaseClient();
            dbClient.readWriteTransaction().run(transaction -> {
                Mutation mutation = Mutation.delete("CartItems", KeySet.singleKey(Key.of(userId)));
                transaction.buffer(mutation);
                return null;
            });
        });
    }

    @Override
    public CompletableFuture<Cart> getCartAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            Cart cart = Cart.newBuilder().setUserId(userId).build();
            DatabaseClient dbClient = getDatabaseClient();
            ResultSet resultSet = dbClient.singleUse().executeQuery(
                    Statement.newBuilder("SELECT productId, quantity FROM CartItems WHERE userId = @userId")
                            .bind("userId").to(userId)
                            .build()
            );
            while (resultSet.next()) {
                CartItem item = CartItem.newBuilder()
                        .setProductId(resultSet.getString("productId"))
                        .setQuantity((int) resultSet.getLong("quantity"))
                        .build();
                cart = cart.toBuilder().addItems(item).build();
            }
            return cart;
        });
    }

    @Override
    public boolean ping() {
        try (Spanner spanner = getSpanner()) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
