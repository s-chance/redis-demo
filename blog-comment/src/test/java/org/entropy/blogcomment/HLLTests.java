package org.entropy.blogcomment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class HLLTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testHyperLogLog() {
        String[] users = new String[1000];
        int index = 0;
        for (int i = 1; i <= 1000_000; i++) {
            users[index++] = "user_" + i;
            // 每1000条发送一次到redis中
            if (i % 1000 == 0) {
                index = 0;
                stringRedisTemplate.opsForHyperLogLog().add("hll1", users);
            }
        }
        // 统计数量
        Long size = stringRedisTemplate.opsForHyperLogLog().size("hll1");
        System.out.println("size = " + size);
    }
}
