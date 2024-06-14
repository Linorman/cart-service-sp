package com.example.cartservicesp.services;


import com.example.cartservicesp.annotations.GrpcService;
import com.example.cartservicesp.cartstore.ICartStore;
import hipstershop.Cart;
import hipstershop.CartServiceGrpc;
import hipstershop.Empty;
import hipstershop.AddItemRequest;
import hipstershop.GetCartRequest;
import hipstershop.EmptyCartRequest;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class CartService extends CartServiceGrpc.CartServiceImplBase {
    @Autowired
    private ICartStore cartStore;

    @Override
    public void addItem(AddItemRequest request, StreamObserver<Empty> responseObserver) {
        cartStore.addItemAsync(request.getUserId(), request.getItem().getProductId(), request.getItem().getQuantity())
                .thenAccept(v -> {
                    responseObserver.onNext(Empty.newBuilder().build());
                    responseObserver.onCompleted();
                })
                .exceptionally(e -> {
                    responseObserver.onError(new RuntimeException("Failed to add item to cart", e));
                    return null;
                });
    }

    @Override
    public void getCart(GetCartRequest request, StreamObserver<Cart> responseObserver) {
        cartStore.getCartAsync(request.getUserId())
                .thenAccept(responseObserver::onNext)
                .thenRun(responseObserver::onCompleted)
                .exceptionally(e -> {
                    responseObserver.onError(new RuntimeException("Failed to get cart", e));
                    return null;
                });
    }

    @Override
    public void emptyCart(EmptyCartRequest request, StreamObserver<Empty> responseObserver) {
        cartStore.emptyCartAsync(request.getUserId())
                .thenAccept(v -> responseObserver.onNext(Empty.newBuilder().build()))
                .thenRun(responseObserver::onCompleted)
                .exceptionally(e -> {
                    responseObserver.onError(new RuntimeException("Failed to empty cart", e));
                    return null;
                });
    }
}
