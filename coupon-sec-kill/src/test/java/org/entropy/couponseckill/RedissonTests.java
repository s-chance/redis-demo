package org.entropy.couponseckill;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
public class RedissonTests {
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedissonClient redissonClient1;

    @Resource
    private RedissonClient redissonClient2;

    @Resource
    private RedissonClient redissonClient3;

    private RLock lock;

    @BeforeEach
    void setUp() {
//        lock = redissonClient.getLock("order");
        RLock lock1 = redissonClient1.getLock("order");
        RLock lock2 = redissonClient2.getLock("order");
        RLock lock3 = redissonClient3.getLock("order");

        // 创建 multiLock
        lock = redissonClient1.getMultiLock(lock1, lock2, lock3);
    }

    @Test
    void m1() throws InterruptedException {
        boolean isLocked = lock.tryLock(1L, TimeUnit.SECONDS);
        if (!isLocked) {
            log.error("获取锁失败 ... 1");
            return;
        }

        try {
            log.info("获取锁成功 ... 1");
            m2();
            log.info("执行业务 ... 1");
        } finally {
            log.warn("准备释放锁 ... 1");
            lock.unlock();
        }
    }

    void m2() {
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.error("获取锁失败 ... 2");
            return;
        }

        try {
            log.info("获取锁成功 ... 2");
            log.info("执行业务 ... 2");
        } finally {
            log.warn("准备释放锁 ... 2");
            lock.unlock();
        }
    }
}
