package org.entropy.blogcomment.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.dto.UserDTO;
import org.entropy.blogcomment.mapper.BlogMapper;
import org.entropy.blogcomment.pojo.Blog;
import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.pojo.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class BlogService extends ServiceImpl<BlogMapper, Blog> {
    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;

    public BlogService(UserService userService, StringRedisTemplate stringRedisTemplate) {
        this.userService = userService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Result<?> queryBlogById(Long id) {
        // 查询
        Blog blog = getById(id);
        if (blog == null) {
            return Result.failure("笔记不存在");
        }
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickname());
        blog.setAvatar(user.getAvatar());

        // 查询blog是否被当前用户点赞
        isBlogLiked(blog, userId);
        return Result.success("查询成功", blog);
    }

    private void isBlogLiked(Blog blog, Long userId) {
        // 判断当前用户是否已经点赞
        String key = "blog:liked:" + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }

    public Result<Void> likeBlog(Long id) {
        // 获取登录用户，这里假设已登录用户id是1
        Long userId = 1L;
        // 判断当前用户是否已经点赞
        String key = "blog:liked:" + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            // 如果未点过赞，则可以点赞
            // 数据库点赞数+1
            boolean result = update().setSql("liked = liked + 1").eq("id", id).update();
            // 保存用户点赞信息到Redis的set集合
            if (result) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
            return Result.success("点赞成功", null);
        } else {
            // 如果已点赞，则取消点赞
            // 数据库点赞数-1
            boolean result = update().setSql("liked = liked - 1").eq("id", id).update();
            // 把用户点赞信息从Redis的set集合中移除
            if (result) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
            return Result.success("取消点赞成功", null);
        }
    }

    public Result<?> queryLikesById(Long id) {
        // 查询top5的点赞用户
        String key = "blog:liked:" + id;
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Result.success("还无用户点赞", Collections.emptyList());
        }
        // 解析其中的用户id
        List<Long> ids = top5.stream().map(Long::valueOf).toList();
        // 根据用户id查询用户
        List<UserDTO> userDTOS = userService.queryUserList(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .toList();
        return Result.success("查询成功", userDTOS);
    }
}
