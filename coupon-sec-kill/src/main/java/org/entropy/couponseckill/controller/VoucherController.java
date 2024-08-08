package org.entropy.couponseckill.controller;

import jakarta.annotation.Resource;
import org.entropy.couponseckill.pojo.Result;
import org.entropy.couponseckill.pojo.Voucher;
import org.entropy.couponseckill.service.VoucherService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private VoucherService voucherService;

    @PostMapping("/seckill")
    public Result<Long> addSecKillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSecKillVoucher(voucher);
        return Result.success("返回成功", voucher.getId());
    }
}
