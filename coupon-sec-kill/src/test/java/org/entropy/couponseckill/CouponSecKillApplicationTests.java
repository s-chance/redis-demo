package org.entropy.couponseckill;

import io.micrometer.observation.Observation;
import jakarta.annotation.Resource;
import org.entropy.couponseckill.utils.RedisIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class CouponSecKillApplicationTests {

    @Resource
    private RedisIdGenerator redisIdGenerator;

    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    void testIdGenerator() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        // 100个id
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdGenerator.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        // 300x100个id
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }

}
