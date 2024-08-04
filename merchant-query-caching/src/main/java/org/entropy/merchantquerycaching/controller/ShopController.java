package org.entropy.merchantquerycaching.controller;

import jakarta.annotation.Resource;
import org.entropy.merchantquerycaching.pojo.Result;
import org.entropy.merchantquerycaching.service.ShopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
