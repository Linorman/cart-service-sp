package com.example.cartservicesp.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RedisAddressCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String redisAddress = System.getenv("REDIS_ADDR");
        return redisAddress != null && !redisAddress.isEmpty();
    }
}
