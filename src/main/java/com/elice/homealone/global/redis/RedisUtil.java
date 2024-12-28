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
        private final RedisTemplate<String, Object> redisTemplate;
        @Value("${spring.jwt.token.access-expiration-time}")
        private long expirationTime;


        /**
         * 시간은 MILLISECONDS 단위로 저장
         */
        public void set(String key, Object value, long milliseconds) {
            redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(value.getClass()));
            redisTemplate.opsForValue().set(key, value, milliseconds, TimeUnit.MILLISECONDS);
        }

        public Object get(String key) {
            return redisTemplate.opsForValue().get(key);
        }

        public boolean delete(String key) {
            return redisTemplate.delete(key);
        }

        public boolean hasKey(String key) {
            return redisTemplate.hasKey(key);
        }

        /**
         * jwt blackList 관리용
         */
        public void setBlackList(String key, Object o) {
            redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(o.getClass()));
            redisTemplate.opsForValue().set(key, o, expirationTime, TimeUnit.MILLISECONDS);
        }

        public boolean hasKeyBlackList(String key) {
            if (redisTemplate == null) {
                throw new HomealoneException(ErrorCode.REDIS_NOT_INITIALIZED);
            }
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        }
    }