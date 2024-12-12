package com.elice.homealone.global.redis;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> redisBlackListTemplate;

    @Value("${spring.jwt.token.access-expiration-time}")
    private long expirationTime;

    public void set(String key, Object value, long milliseconds) {
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(value.getClass()));
        redisTemplate.opsForValue().set(key, value, milliseconds, TimeUnit.MILLISECONDS);
    }

    public void setBlackList(String key, Object o) {
        redisBlackListTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(o.getClass()));
        redisBlackListTemplate.opsForValue().set(key, o, expirationTime, TimeUnit.MILLISECONDS);
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
        // 직렬화 설정
        Jackson2JsonRedisSerializer<T> serializer = new Jackson2JsonRedisSerializer<>((Class<T>) value.getClass());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);

        // 데이터 저장
        redisTemplate.opsForValue().set(key, value, milliseconds, TimeUnit.MILLISECONDS);
    }

    public <T> T cachingGet(String key, Class<T> clazz) {
        // 직렬화 설정
        Jackson2JsonRedisSerializer<T> serializer = new Jackson2JsonRedisSerializer<>(clazz);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);

        // 데이터 읽기
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return clazz.cast(value);
        }
        return null;
    }
}