package com.example.cartservicesp.config;


import com.example.cartservicesp.cartstore.AlloyDBCartStore;
import com.example.cartservicesp.cartstore.ICartStore;
import com.example.cartservicesp.cartstore.InMemoryCartStore;
import com.example.cartservicesp.cartstore.RedisCartStore;
import com.example.cartservicesp.cartstore.SpannerCartStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CartStoreConfig {
    @Bean
    public ICartStore cartStore() {
        String redisAddress = System.getenv("REDIS_ADDR");
        String spannerProjectId = System.getenv("SPANNER_PROJECT_ID");
        String spannerConnectionString = System.getenv("SPANNER_CONNECTION_STRING");
        String alloydbConnectionString = System.getenv("ALLOYDB_CONNECTION_STRING");

        if (redisAddress != null && !redisAddress.isEmpty()) {
            return new RedisCartStore();
        }
        if (spannerProjectId != null && !spannerProjectId.isEmpty() && spannerConnectionString != null && !spannerConnectionString.isEmpty()) {
            return new SpannerCartStore();
        }
        if (alloydbConnectionString != null && !alloydbConnectionString.isEmpty()) {
            return new AlloyDBCartStore();
        }
        return new InMemoryCartStore();
    }
}
