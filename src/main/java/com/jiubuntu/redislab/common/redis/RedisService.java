package com.jiubuntu.redislab.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        return type.isInstance(value) ? (T) value : null;
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean expire(String key, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, ttl));
    }

    public void leftPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    public Long remove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    public void trim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    public List<Object> range(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public long getCounter(String key) {
        // INCR로 쌓인 값은 역직렬화 시 Integer/Long 등 실제 숫자 타입이 그때그때 달라질 수 있어 get(key, Class)로는 못 받는다.
        Object value = redisTemplate.opsForValue().get(key);
        return value instanceof Number number ? number.longValue() : 0L;
    }

    public Long addToSet(String key, Object value) {
        return redisTemplate.opsForSet().add(key, value);
    }
}
