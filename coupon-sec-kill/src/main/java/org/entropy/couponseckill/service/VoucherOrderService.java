package org.entropy.couponseckill.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.entropy.couponseckill.mapper.VoucherOrderMapper;
import org.entropy.couponseckill.pojo.Result;
import org.entropy.couponseckill.pojo.VoucherOrder;
import org.entropy.couponseckill.utils.RedisIdGenerator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
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

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("scripts/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    // 在类初始化之后立即执行线程池
    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {
        String queueName = "stream.orders";

        @Override
        public void run() {
            while (true) {
                try {
                    // 获取消息队列中的订单信息
                    // XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS stream.orders >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );
                    // 判断消息是否获取成功
                    if (list == null || list.isEmpty()) {
                        // 获取失败，队列中没有消息，继续下一次循环
                        continue;
                    }
                    // 解析订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    // 获取成功，创建订单
                    handleVoucherOrder(voucherOrder);
                    // ACK确认
                    // SACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("订单处理异常:", e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    // 获取pending-list中的订单信息
                    // XREADGROUP GROUP g1 c1 COUNT 1 STREAMS stream.orders 0
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );
                    // 判断消息是否获取成功
                    if (list == null || list.isEmpty()) {
                        // 获取失败，pending-list中没有异常消息，结束循环
                        break;
                    }
                    // 解析订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    // 获取成功，创建订单
                    handleVoucherOrder(voucherOrder);
                    // ACK确认
                    // SACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("pending-list处理异常:", e);
                    try {
                        TimeUnit.MILLISECONDS.sleep(20);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }

    /*private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    // 1.获取队列中的订单信息
                    VoucherOrder voucherOrder = orderTasks.take();
                    // 2.创建订单
                    handleVoucherOrder(voucherOrder);
                } catch (Exception e) {
                    log.error("订单处理异常:", e);
                }
            }
        }*/

        private void handleVoucherOrder(VoucherOrder voucherOrder) {
            // 获取用户id
            Long userId = voucherOrder.getUserId();

            // 创建锁对象
            RLock lock = redissonClient.getLock("lock:voucher-order:" + userId);
            // 获取锁
            boolean isLocked = lock.tryLock();
            // 判断是否获取成功
            if (!isLocked) {
                // 获取失败，重试或返回报错
                log.error("不允许重复购买");
                return;
            }

            try {
                // 异步线程中无法直接获取主线程的代理对象，可以通过传参或成员变量的方式获取代理对象
                proxy.createVoucherOrder(voucherOrder);
            } catch (IllegalStateException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }

    // 代理对象，供异步线程使用
    private VoucherOrderService proxy;

    public Result<?> secKillVoucher(Long voucherId) {
        // 用户id
        Long userId = 1L;
        // 订单id
        long orderId = redisIdGenerator.nextId("order");
        // 1.执行Lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );

        // 2.判断结果是否为0
        if (result == null) {
            throw new RuntimeException("null result");
        }
        int r = result.intValue();
        if (r != 0) {
            // 2.1.不为0，代表不符合购买条件
            return Result.failure(r == 1 ? "库存不足" : "不可重复购买");
        }
        // 获取增强的代理对象
        proxy = (VoucherOrderService) (AopContext.currentProxy());

        // 3.返回订单id
        return Result.success("抢购成功", orderId);
    }

    /*public Result<?> secKillVoucher(Long voucherId) {
        Long userId = 1L;
        // 1.执行Lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString()
        );

        // 2.判断结果是否为0
        if (result == null) {
            throw new RuntimeException("null result");
        }
        int r = result.intValue();
        if (r != 0) {
            // 2.1.不为0，代表不符合购买条件
            return Result.failure(r == 1 ? "库存不足" : "不可重复购买");
        }
        // 2.2.为0，代表符合购买条件，保存订单信息到阻塞队列
        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisIdGenerator.nextId("order");
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        // 2.3.获取增强的代理对象，供异步线程使用
        proxy = (VoucherOrderService) (AopContext.currentProxy());
        // 2.4.保存到阻塞队列
        orderTasks.add(voucherOrder);

        // 3.返回订单id
        return Result.success("抢购成功", orderId);
    }*/

    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 一人一单
        Long userId = voucherOrder.getUserId();
        // 查询订单
        Long count = query()
                .eq("user_id", userId)
                .eq("voucher_id", voucherOrder.getVoucherId())
                .count();
        // 判断是否重复购买
        if (count > 0) {
            // 用户已经购买过了
            log.error("不可重复购买");
            return;
        }

        // 扣减库存
        boolean res = secKillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0)
                .update();
        if (!res) {
            // 扣减失败
            log.error("库存不足");
            return;
        }
        // 创建订单
        save(voucherOrder);
    }


    /*public Result<?> secKillVoucher(Long voucherId) {
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
    }*/

    /*@Transactional
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
    }*/
}
