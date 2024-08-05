package org.entropy.merchantquerycaching.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.entropy.merchantquerycaching.pojo.Result;
import org.entropy.merchantquerycaching.pojo.Shop;
import org.entropy.merchantquerycaching.pojo.ShopType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.entropy.merchantquerycaching.constant.RedisConstants.*;

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

    public Result<?> queryById(Long id) {
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

        // 4.不存在，根据id查询数据库
        Shop shop = shopDB.get(id);
        // 模拟数据库查询耗时
        try {
            TimeUnit.SECONDS.sleep(1);
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

        // 7.返回
        return Result.success("返回成功", shop);
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
