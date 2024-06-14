package com.example.cartservicesp.utils;


import com.google.protobuf.Message;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class ProtobufRedisSerializer<T extends Message> implements RedisSerializer<T> {

    private final Class<T> clazz;

    public ProtobufRedisSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        return t == null ? new byte[0] : t.toByteArray();
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        try {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            return (T) clazz.getMethod("parseFrom", byte[].class).invoke(null, bytes);
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize Protobuf message", e);
        }
    }
}

