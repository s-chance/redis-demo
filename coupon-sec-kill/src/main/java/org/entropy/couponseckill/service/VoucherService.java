package org.entropy.couponseckill.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.couponseckill.mapper.VoucherMapper;
import org.entropy.couponseckill.pojo.SecKillVoucher;
import org.entropy.couponseckill.pojo.Voucher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoucherService extends ServiceImpl<VoucherMapper, Voucher> {


    private final SecKillVoucherService secKillVoucherService;

    public VoucherService(SecKillVoucherService secKillVoucherService) {
        this.secKillVoucherService = secKillVoucherService;
    }

    @Transactional
    public void addSecKillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SecKillVoucher secKillVoucher = new SecKillVoucher();
        secKillVoucher.setVoucherId(voucher.getId());
        secKillVoucher.setStock(voucher.getStock());
        secKillVoucher.setBeginTime(voucher.getBeginTime());
        secKillVoucher.setEndTime(voucher.getEndTime());
        secKillVoucherService.save(secKillVoucher);
    }
}
