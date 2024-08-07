package org.entropy.merchantquerycaching.service;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.entropy.merchantquerycaching.pojo.RedisData;
import org.entropy.merchantquerycaching.pojo.Result;
import org.entropy.merchantquerycaching.pojo.Shop;
import org.entropy.merchantquerycaching.pojo.ShopType;
import org.entropy.merchantquerycaching.utils.CacheClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.entropy.merchantquerycaching.constant.RedisConstants.*;

@Slf4j
@Service
public class ShopService {

    private final Map<Long, Shop> shopDB = new HashMap<>() {{
        put(1L, new Shop("1121233", "fruit"));
        put(2L, new Shop("7799898", "vegetable"));
    }};

    private final List<ShopType> shopTypeDB = new ArrayList<>() {{
        add(new ShopType(1L, "food", "食品"));
        add(new ShopType(2L, "drink", "饮料"));
        add(new ShopType(3L, "game", "游戏"));
    }};

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    public Result<?> queryById(Long id) {
        // 调用工具类的预防缓存穿透的数据获取方法
//        Shop shop = cacheClient.getWithPassThrough(CACHE_SHOP_KEY_PREFIX, id, Shop.class, shopDB::get,
//                CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 调用工具类的预防缓存击穿的逻辑过期数据获取方法
        Shop shop = cacheClient.getWithLogicalExpire(CACHE_SHOP_KEY_PREFIX, id, Shop.class, shopDB::get,
                30L, TimeUnit.SECONDS);

        if (shop == null) {
            return Result.failure("对应的数据不存在");
        }

        return Result.success("返回成功", shop);

//        return queryWithMutex(id);

        // 使用逻辑过期需要提前将热点数据手动导入缓存
//        return queryWithLogicalExpire(id);
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    private Result<?> queryWithLogicalExpire(Long id) {
        // 1.从redis中查询缓存
        String key = CACHE_SHOP_KEY_PREFIX + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否命中
        if (StrUtil.isBlank(shopJson)) {
            // 3.未命中，直接返回空值
            return Result.failure("该商户不是活动店铺");
        }
        // 4.命中，将json反序列化为对象后再进行判断
        RedisData<?> redisData = JSONUtil.toBean(shopJson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5.判断是否逻辑过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1.未过期，返回商户信息
            return Result.success("操作成功", shop);
        }
        // 5.2.已过期，重建缓存
        // 6.缓存重建
        // 6.1.获取互斥锁
        String lockKey = LOCK_SHOP_KEY_PREFIX + id;
        boolean isLocked = lock(lockKey);
        // 6.2.判断是否成功获取互斥锁
        if (isLocked) {
            // 双重检查锁，重新检查redis缓存
            shopJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(shopJson)) {
                redisData = JSONUtil.toBean(shopJson, RedisData.class);
                shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
                expireTime = redisData.getExpireTime();
                if (expireTime.isAfter(LocalDateTime.now())) {
                    return Result.success("返回成功", shop);
                }
            }
            // 6.3.成功，开启独立线程，重建缓存
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 重建缓存
                    saveShop2Redis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }
        // 6.4.返回过期的信息
        return Result.success("返回成功，但数据可能不是最新的", shop);
    }

    private Result<?> queryWithMutex(Long id) {
        // 1.从redis中查询缓存
        String key = CACHE_SHOP_KEY_PREFIX + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.存在，反序列化后返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            // 刷新有效时间
            stringRedisTemplate.expire(key, CACHE_SHOP_TTL, TimeUnit.MINUTES);
            return Result.success("操作成功", shop);
        }
        // 判断命中的是否是空值
        if (shopJson != null && shopJson.isEmpty()) {
            // 返回空值信息
            return Result.failure("商户不存在");
        }

        // 4.实现缓存重建
        // 4.1.获取互斥锁
        String lockKey = "lock:shop:" + id;
        Shop shop = null;
        try {
            boolean isLocked = lock(lockKey);
            // 4.2.判断是否获取成功
            if (!isLocked) {
                // 4.3.失败，则休眠一段时间后再重试
                TimeUnit.MILLISECONDS.sleep(50);
                return queryById(id);
            }

            // 4.4.成功
            // 双重检查锁，重新检查redis缓存
            shopJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(shopJson)) {
                // 已经存在缓存，无需重建
                shop = JSONUtil.toBean(shopJson, Shop.class);
                stringRedisTemplate.expire(key, CACHE_SHOP_TTL, TimeUnit.MINUTES);
                return Result.success("操作成功", shop);
            }

            // 根据id查询数据库
            shop = shopDB.get(id);
            // 模拟数据库查询耗时
            try {
                TimeUnit.SECONDS.sleep(1);
                log.info("数据库查询完成");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 5.查询不到，返回提示信息
            if (shop == null) {
                // 将空值写入redis
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                // 返回提示信息
                return Result.failure("商户不存在");
            }
            // 6.查询到了，序列化并写入redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7.释放互斥锁
            unlock(lockKey);
        }
        // 8.返回
        return Result.success("返回成功", shop);
    }

    private boolean lock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    public void saveShop2Redis(Long id, Long expireSeconds) {
        // 1.查询商铺数据
        Shop shop = shopDB.get(id);
        // 模拟缓存重建耗时
        try {
            TimeUnit.MILLISECONDS.sleep(200);
            log.info("重建缓存中...");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 2.封装逻辑过期时间
        RedisData<Shop> shopRedisData = new RedisData<>();
        shopRedisData.setData(shop);
        shopRedisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // 3.写入redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY_PREFIX + id, JSONUtil.toJsonStr(shopRedisData));
    }

    public Result<?> queryType() {
        List<String> typeList = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        if (typeList != null && !typeList.isEmpty()) {
            List<ShopType> shopTypes = typeList.stream()
                    .map(type -> JSONUtil.toBean(type, ShopType.class))
                    .collect(Collectors.toList());
            return Result.success("操作成功", shopTypes);
        }

        // 模拟数据库查询耗时
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (shopTypeDB.isEmpty()) {
            return Result.failure("没有数据");
        }

        List<String> shopTypeList = shopTypeDB.stream()
                .map(JSONUtil::toJsonStr)
                .collect(Collectors.toList());

        stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY, shopTypeList);

        return Result.success("返回成功", shopTypeDB);
    }

    @Transactional
    public Result<?> updateById(Long id, Shop shop) {
        // 1.更新数据库
        // 模拟数据库更新耗时
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        shopDB.put(id, shop);

        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY_PREFIX + id);

        // 如果是分布式系统，删除缓存的操作可能会用mq异步通知对应的服务去执行，并且需要使用分布式事务方案

        return Result.success("操作成功", null);
    }
}
