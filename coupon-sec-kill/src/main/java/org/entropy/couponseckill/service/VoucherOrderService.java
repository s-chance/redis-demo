package org.entropy.couponseckill.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.couponseckill.mapper.VoucherOrderMapper;
import org.entropy.couponseckill.pojo.Result;
import org.entropy.couponseckill.pojo.SecKillVoucher;
import org.entropy.couponseckill.pojo.VoucherOrder;
import org.entropy.couponseckill.utils.RedisIdGenerator;
import org.entropy.couponseckill.utils.SimpleRedisLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VoucherOrderService extends ServiceImpl<VoucherOrderMapper, VoucherOrder> {

    private final SecKillVoucherService secKillVoucherService;
    private final RedisIdGenerator redisIdGenerator;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    public VoucherOrderService(SecKillVoucherService secKillVoucherService, RedisIdGenerator redisIdGenerator, StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.secKillVoucherService = secKillVoucherService;
        this.redisIdGenerator = redisIdGenerator;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    public Result<?> secKillVoucher(Long voucherId) {
        // 1.查询优惠券
        SecKillVoucher voucher = secKillVoucherService.getById(voucherId);
        // 2.判断秒杀是否开始或结束
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 尚未开始
            return Result.failure("秒杀活动尚未开始");
        }
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 已经结束
            return Result.failure("秒杀活动已经结束");
        }

        // 3.判断是否还有库存
        if (voucher.getStock() < 1) {
            // 库存不足
            return Result.failure("库存不足");
        }

        Long userId = 1L;
        // 创建分布式锁对象
//        SimpleRedisLock lock = new SimpleRedisLock("voucher-order:" + userId, stringRedisTemplate);
        RLock lock = redissonClient.getLock("lock:voucher-order:" + userId);
        // 获取锁
//        boolean isLocked = lock.lock(5);
        boolean isLocked = lock.tryLock();
        // 判断是否获取成功
        if (!isLocked) {
            // 获取失败，重试或返回报错
            return Result.failure("不可重复购买");

        }

        try {
            // 获取增强的代理对象
            VoucherOrderService proxy = (VoucherOrderService) (AopContext.currentProxy());
            return proxy.createVoucherOrder(voucherId);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

//        synchronized (userId.toString().intern()) {
//            VoucherOrderService proxy = (VoucherOrderService) (AopContext.currentProxy());
//            return proxy.createVoucherOrder(voucherId);
//        }
    }

    @Transactional
    public Result<?> createVoucherOrder(Long voucherId) {
        // 一人一单
        Long userId = 1L;
        // 查询订单
        Long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        // 判断是否重复购买
        if (count > 0) {
            // 用户已经购买过了
            return Result.failure("不可重复购买");
        }

        // 4.扣减库存
        boolean res = secKillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (!res) {
            // 扣减失败
            return Result.failure("库存不足");
        }
        // 5.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 5.1.订单id
        long orderId = redisIdGenerator.nextId("order");
        voucherOrder.setId(orderId);
        // 5.2.用户id
        voucherOrder.setUserId(userId);

        // 5.3.代金券id
        voucherOrder.setVoucherId(voucherId);

        save(voucherOrder);

        // 6.返回订单id
        return Result.success("返回成功", voucherId);
    }
}
