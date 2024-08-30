package org.entropy.blogcomment.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.mapper.UserMapper;
import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.pojo.User;
import org.springframework.data.redis.connection.BitFieldSubCommands;
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

    public Result<Integer> signCount() {
        // 假设当前登录用户id为1
        Long userId = 1L;
        // 获取日期
        LocalDateTime now = LocalDateTime.now();
        // 拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = "sign:" + userId + keySuffix;
        // 获取今日是本月第几天
        int dayOfMonth = now.getDayOfMonth();
        // 获取本月截止到今日的所有签到记录，返回的是十进制数字，需要进行位运算
        List<Long> results = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                        .valueAt(0)
        );
        if (results == null || results.isEmpty()) {
            return Result.success("暂无记录", null);
        }
        Long result = results.get(0);
        if (result == null || result == 0) {
            return Result.success("暂无记录", null);
        }
        // 今日还未签到则先跳过统计今日
        if ((result & 1) == 0) result >>>= 1;

        // 循环遍历
        int count = 0;
        while (result > 0) {
            // 与运算获取bit位
            // 判断bit位是否为0
            if ((result & 1) == 0) {
                // 为0，未签到，结束统计
                break;
            } else {
                // 不为0，继续统计，计数器+1
                count++;
                // 数字右移1位
                result >>>= 1;
            }
        }
        return Result.success("查询成功", count);
    }
}
