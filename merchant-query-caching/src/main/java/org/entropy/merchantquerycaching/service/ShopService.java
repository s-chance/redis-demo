package org.entropy.merchantquerycaching.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.entropy.merchantquerycaching.pojo.Result;
import org.entropy.merchantquerycaching.pojo.Shop;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.entropy.merchantquerycaching.constant.RedisConstants.CACHE_SHOP_KEY_PREFIX;

@Service
public class ShopService {

    private final Map<Long, Shop> shopDB = new HashMap<>() {{
        put(1L, new Shop("1121233", "fruit"));
        put(2L, new Shop("7799898", "vegetable"));
    }};

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public Result<?> queryById(Long id) {
        // 1.从redis中查询缓存
        String key = CACHE_SHOP_KEY_PREFIX + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.存在，反序列后返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.success("操作成功", shop);
        }
        // 4.不存在，根据id查询数据库
        Shop shop = shopDB.get(id);
        // 模拟数据库查询耗时
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (shop == null) {
            // 5.查询不到，返回
            return Result.failure("商户不存在");
        }
        // 6.查询到了，序列化并写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop));

        // 7.返回
        return Result.success("返回成功", shop);
    }
}
