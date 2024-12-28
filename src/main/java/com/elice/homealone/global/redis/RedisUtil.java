    package com.elice.homealone.global.redis;


    import com.elice.homealone.global.exception.ErrorCode;
    import com.elice.homealone.global.exception.HomealoneException;
    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.data.redis.core.RedisTemplate;
    import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
    import org.springframework.stereotype.Component;

    import java.util.concurrent.TimeUnit;

    @Component
    @RequiredArgsConstructor
    public class RedisUtil {
        private final RedisTemplate<String, Object> redisBlackListTemplate;
        @Value("${spring.jwt.token.access-expiration-time}")
        private long expirationTime;

        public void setBlackList(String key, Object o) {
            redisBlackListTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(o.getClass()));
            redisBlackListTemplate.opsForValue().set(key, o, expirationTime, TimeUnit.MILLISECONDS);
        }

        public boolean hasKeyBlackList(String key) {
            if (redisBlackListTemplate == null) {
                throw new HomealoneException(ErrorCode.REDIS_NOT_INITIALIZED);
            }
            return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(key));
        }
    }