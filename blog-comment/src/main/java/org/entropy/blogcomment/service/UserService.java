package org.entropy.blogcomment.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.mapper.UserMapper;
import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.pojo.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public UserService(UserMapper userMapper, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public List<User> queryUserList(List<Long> ids) {
        return userMapper.queryUserList(ids);
    }

    public Result<Void> sign() {
        // 假设当前登录用户id为1
        Long userId = 1L;
        // 获取日期
        LocalDateTime now = LocalDateTime.now();
        // 拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = "sign:" + userId + keySuffix;
        // 获取今日是本月第几天
        int dayOfMonth = now.getDayOfMonth();
        // 写入Redis
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.success("签到成功", null);
    }
}
