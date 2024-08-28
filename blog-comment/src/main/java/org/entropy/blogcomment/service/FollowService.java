package org.entropy.blogcomment.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.dto.UserDTO;
import org.entropy.blogcomment.mapper.FollowMapper;
import org.entropy.blogcomment.pojo.Follow;
import org.entropy.blogcomment.pojo.Result;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class FollowService extends ServiceImpl<FollowMapper, Follow> {
    private final StringRedisTemplate stringRedisTemplate;
    private final UserService userService;

    public FollowService(StringRedisTemplate stringRedisTemplate, UserService userService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userService = userService;
    }

    public Result<Void> follow(Long followUerId) {
        // 假设当前用户id为1
        Long userId = 1L;
        // 判断是关注还是取关
        boolean exists = query().eq("user_id", userId).eq("follow_user_id", followUerId).exists();
        String key = "follows:" + userId;
        if (!exists) {
            // 关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUerId);
            boolean result = save(follow);
            if (result) {
                // 把关注用户的id放入Redis的set集合
                stringRedisTemplate.opsForSet().add(key, followUerId.toString());
            }
            return Result.success("关注成功", null);
        } else {
            // 取关，删除数据
            boolean result = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId)
                    .eq("follow_user_id", followUerId)
            );
            if (result) {
                // 把关注用户的id从Redis的set集合中移除
                stringRedisTemplate.opsForSet().remove(key, followUerId.toString());
            }
            return Result.success("取关成功", null);
        }
    }

    public Result<Boolean> followStatus(Long followUerId) {
        // 假设当前用户id为1
        Long userId = 1L;
        // 查询关注情况
        boolean exists = query().eq("user_id", userId).eq("follow_user_id", followUerId).exists();
        // 判断
        return Result.success("查询成功", exists);
    }

    public Result<?> followCommon(Long id) {
        // 假设当前用户id为1
        Long userId = 1L;
        // 求交集
        String key = "follows:" + userId;
        String key2 = "follows:" + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        if (intersect == null || intersect.isEmpty()) {
            return Result.success("无共同关注", Collections.emptyList());
        }
        // 解析id集合
        List<Long> ids = intersect.stream().map(Long::valueOf).toList();
        List<UserDTO> userDTOS = userService.listByIds(ids).stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .toList();
        return Result.success("查询成功", userDTOS);
    }
}
