package com.elice.homealone.global.redis;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> redisBlackListTemplate;
    @Value("${spring.jwt.token.access-expiration-time}")
    private long expirationTime;
    private final ObjectMapper objectMapper;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate,
                     RedisTemplate<String, Object> redisBlackListTemplate,
                     ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.redisBlackListTemplate = redisBlackListTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        this.redisBlackListTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisBlackListTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
    }

    public void set(String key, Object value, long milliseconds) {
        redisTemplate.opsForValue().set(key, value, milliseconds, TimeUnit.MILLISECONDS);
    }

    public void setBlackList(String key, Object value) {
        redisBlackListTemplate.opsForValue().set(key, value, expirationTime, TimeUnit.MILLISECONDS);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Object getBlackList(String key) {
        return redisBlackListTemplate.opsForValue().get(key);
    }

    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public boolean deleteBlackList(String key) {
        return redisBlackListTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean hasKeyBlackList(String key) {
        if (redisBlackListTemplate == null) {
            throw new HomealoneException(ErrorCode.REDIS_NOT_INITIALIZED);
        }
        return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(key));
    }

    public <T> void cachingSet(String key, T value, long milliseconds) {
        redisTemplate.opsForValue().set(key, value, milliseconds, TimeUnit.MILLISECONDS);
    }

    public <T> T cachingGet(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return objectMapper.convertValue(value, clazz);
        }
        return null;
    }
}
