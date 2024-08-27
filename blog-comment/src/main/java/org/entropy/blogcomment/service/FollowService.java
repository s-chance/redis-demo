package org.entropy.blogcomment.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.mapper.FollowMapper;
import org.entropy.blogcomment.pojo.Follow;
import org.entropy.blogcomment.pojo.Result;
import org.springframework.stereotype.Service;

@Service
public class FollowService extends ServiceImpl<FollowMapper, Follow> {
    public Result<Void> follow(Long followUerId) {
        // 假设当前用户id为1
        Long userId = 1L;
        // 判断是关注还是取关
        boolean exists = query().eq("user_id", userId).eq("follow_user_id", followUerId).exists();
        if (!exists) {
            // 关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUerId);
            save(follow);
            return Result.success("关注成功", null);
        } else {
            // 取关，删除数据
            remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId)
                    .eq("follow_user_id", followUerId)
            );
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
}
