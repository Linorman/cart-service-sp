package com.example.cartservicesp.config;


import com.example.cartservicesp.utils.ProtobufRedisSerializer;
import hipstershop.Cart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    @Conditional(RedisAddressCondition.class)
    public RedisConnectionFactory redisConnectionFactory() {
        String redisAddress = System.getenv("REDIS_ADDR");
        if (redisAddress == null || redisAddress.isEmpty()) {
            return null;
        }
        String redisHost = redisAddress.split(":")[0];
        int redisPort = Integer.parseInt(redisAddress.split(":")[1]);
        String redisPassword = System.getenv("REDIS_PASSWORD");

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(RedisPassword.of(redisPassword));
        }

        return new JedisConnectionFactory(config);
    }

    @Bean
    @Conditional(RedisAddressCondition.class)
    public RedisTemplate<String, Cart> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        if (redisConnectionFactory == null) {
            return null;
        }
        RedisTemplate<String, Cart> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new ProtobufRedisSerializer<>(Cart.class));
        return template;
    }
}

