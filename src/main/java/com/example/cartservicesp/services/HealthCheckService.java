package com.example.cartservicesp.services;


import com.example.cartservicesp.annotations.GrpcService;
import com.example.cartservicesp.cartstore.ICartStore;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class HealthCheckService extends HealthGrpc.HealthImplBase {

    @Autowired
    private ICartStore cartStore;

    @Override
    public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
        HealthCheckResponse.ServingStatus status = cartStore.ping() ?
                HealthCheckResponse.ServingStatus.SERVING :
                HealthCheckResponse.ServingStatus.NOT_SERVING;
        responseObserver.onNext(HealthCheckResponse.newBuilder().setStatus(status).build());
        responseObserver.onCompleted();
    }
}

