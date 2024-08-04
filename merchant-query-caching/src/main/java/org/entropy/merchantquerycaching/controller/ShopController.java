package org.entropy.merchantquerycaching.controller;

import jakarta.annotation.Resource;
import org.entropy.merchantquerycaching.pojo.Result;
import org.entropy.merchantquerycaching.pojo.Shop;
import org.entropy.merchantquerycaching.service.ShopService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    private ShopService shopService;

    /**
     * 根据id查询商户信息
     * @param id 商户id
     * @return 商户详情数据
     */
    @GetMapping("/{id}")
    public Result<?> queryShopById(@PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    /**
     * 根据id更新商铺信息
     * @param id 商铺id
     * @param shop 商铺数据
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<?> updateShop(@PathVariable("id") Long id, @RequestBody Shop shop) {
        return shopService.updateById(id, shop);
    }

    /**
     * 查询所有商户类型
     * @return 商户类型列表
     */
    @GetMapping("/type")
    public Result<?> queryShopType() {
        return shopService.queryType();
    }
}
