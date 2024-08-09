package org.entropy.couponseckill.controller;

import org.entropy.couponseckill.pojo.Result;
import org.entropy.couponseckill.service.VoucherOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {


    private final VoucherOrderService voucherOrderService;

    public VoucherOrderController(VoucherOrderService voucherOrderService) {
        this.voucherOrderService = voucherOrderService;
    }

    @PostMapping("/seckill/{id}")
    public Result<?> secKillVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.secKillVoucher(voucherId);
    }
}
