package org.entropy.blogcomment.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.mapper.ShopMapper;
import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.pojo.Shop;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShopService extends ServiceImpl<ShopMapper, Shop> {
    private final StringRedisTemplate stringRedisTemplate;

    public ShopService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Result<?> queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        // 判断是否需要使用坐标查询
        if (x == null || y == null) {
            // 没有坐标，查询数据库
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, 3));
            return Result.success("查询数据库成功", page.getRecords());
        }
        // 计算分页参数
        int start = (current - 1) * 3;
        int end = current * 3;

        // 查询redis，按照距离排序、分页
        String key = "shop:geo:" + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo() // GEOSEARCH key BYLONLAT x y BYRADIUS 10 WITHDIST
                .search(
                        key,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(5000),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );

        // 解析出查询到的id
        if (results == null || results.getContent().size() <= start) {
            return Result.success("暂无信息", Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        // 手动截取分页的数据部分
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distMap = new HashMap<>(list.size());
        list.stream().skip(start).forEach(result -> {
            // 获取店铺id
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            // 获取距离
            Distance distance = result.getDistance();
            distMap.put(shopIdStr, distance);
        });

        // 根据id查询Shop
        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        shops.forEach(shop ->
                shop.setDistance(distMap.get(shop.getId().toString()).getValue()));
        // 返回
        return Result.success("查询成功", shops);
    }
}
