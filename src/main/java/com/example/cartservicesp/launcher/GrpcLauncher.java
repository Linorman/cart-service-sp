package com.example.cartservicesp.launcher;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component("grpcLauncher")
public class GrpcLauncher {
    private Server server;
    @Value("${grpc.server.port}")
    private Integer grpcServerPort;

    public void grpcStart(Map<String, Object> grpcServiceBeanMap) {
        try{
            ServerBuilder serverBuilder = ServerBuilder.forPort(grpcServerPort);
            for (Object bean : grpcServiceBeanMap.values()){
                serverBuilder.addService((BindableService) bean);
            }
            server = serverBuilder.build().start();
            System.out.println("Grpc server start at port: " + grpcServerPort);
            server.awaitTermination();
            Runtime.getRuntime().addShutdownHook(new Thread(this::grpcStop));
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    private void grpcStop(){
        if (server != null){
            server.shutdownNow();
        }
    }
}
