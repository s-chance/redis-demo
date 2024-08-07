package org.entropy.merchantquerycaching.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.entropy.merchantquerycaching.pojo.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.entropy.merchantquerycaching.constant.RedisConstants.LOCK_SHOP_KEY_PREFIX;
import static org.entropy.merchantquerycaching.constant.RedisConstants.LOCK_SHOP_TTL;

@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public <T> void set(String key, T value, Long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, timeUnit);
    }

    public <T> void setWithLogicalExpire(String key, T value, Long time, TimeUnit timeUnit) {
        RedisData<T> redisData = new RedisData<>();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R, ID> R getWithPassThrough(String prefix, ID id, Class<R> type, Function<ID, R> dbCallback,
                                        Long time, TimeUnit timeUnit) {
        String key = prefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            stringRedisTemplate.expire(key, time, timeUnit);
            return JSONUtil.toBean(json, type);
        }

        if (json != null && json.isEmpty()) {
            return null;
        }

        R r = dbCallback.apply(id);
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key, "", time, timeUnit);
            return null;
        }

        set(key, r, time, timeUnit);

        return r;
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public <R, ID> R getWithLogicalExpire(String prefix, ID id, Class<R> type, Function<ID, R> dbCallback,
                                          Long time, TimeUnit timeUnit) {
        String key = prefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }

        RedisData<?> redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();

        if (expireTime.isAfter(LocalDateTime.now())) {
            return r;
        }

        String lockKey = LOCK_SHOP_KEY_PREFIX + id;
        boolean isLocked = lock(lockKey);
        if (isLocked) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    R r1 = dbCallback.apply(id);
                    this.setWithLogicalExpire(key, r1, time, timeUnit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unlock(lockKey);
                }
            });
        }

        return r;
    }


    private boolean lock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
