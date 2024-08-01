package org.entropy.springdataredisdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.entropy.springdataredisdemo.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class SpringDataRedisDemoApplicationTests {

    @Autowired
    @Qualifier("jsonRedis")
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testString() {
        redisTemplate.opsForValue().set("name", "李四");
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println("name = " + name);
    }

    @Test
    void testSaveUser() {
        redisTemplate.opsForValue().set("user:100", new User("Tom", 21));
        User user = (User) redisTemplate.opsForValue().get("user:100");
        System.out.println("user = " + user);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testStringTemplate() throws JsonProcessingException {
        User user = new User("Jerry", 17);
        String json = mapper.writeValueAsString(user);
        stringRedisTemplate.opsForValue().set("user:200", json);
        String val = stringRedisTemplate.opsForValue().get("user:200");
        User value = mapper.readValue(val, User.class);
        System.out.println("user= " + value);
    }

}
