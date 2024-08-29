package org.entropy.blogcomment;

import org.entropy.blogcomment.pojo.Shop;
import org.entropy.blogcomment.service.ShopService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class GEOTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ShopService shopService;

    @Test
    public void loadShopData() {
        // 查询店铺信息
        List<Shop> shops = shopService.list();
        // 将店铺按照typeId分组，存放到不同的集合
        Map<Integer, List<Shop>> map = shops.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        // 分批写入redis
        map.forEach((k, v) -> {
            String key = "shop:geo:" + k;
//            v.forEach(shop -> stringRedisTemplate.opsForGeo().add(key,
//                    new Point(shop.getX().doubleValue(), shop.getY().doubleValue()),
//                    shop.getId().toString()));

            // 减少了对redis的调用次数，优化性能
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(v.size());
            v.forEach(shop -> locations.add(new RedisGeoCommands.GeoLocation<>(
                    shop.getId().toString(),
                    new Point(shop.getX().doubleValue(), shop.getY().doubleValue())))
            );
            // 批量写入redis
            stringRedisTemplate.opsForGeo().add(key, locations);
        });
    }
}
