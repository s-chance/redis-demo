package org.entropy.merchantquerycaching;

import jakarta.annotation.Resource;
import org.entropy.merchantquerycaching.service.ShopService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MerchantQueryCachingApplicationTests {

    @Resource
    private ShopService shopService;

    @Test
    void contextLoads() {
        shopService.saveShop2Redis(1L, 30L);
    }

}
