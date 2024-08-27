package org.entropy.blogcomment.service;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.mapper.BlogMapper;
import org.entropy.blogcomment.pojo.Blog;
import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.pojo.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        blog.setIsLike(BooleanUtil.isTrue(isMember));
    }

    public Result<Void> likeBlog(Long id) {
        // 获取登录用户，这里假设已登录用户id是1
        Long userId = 1L;
        // 判断当前用户是否已经点赞
        String key = "blog:liked:" + id;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        if (BooleanUtil.isFalse(isMember)) {
            // 如果未点过赞，则可以点赞
            // 数据库点赞数+1
            boolean result = update().setSql("liked = liked + 1").eq("id", id).update();
            // 保存用户点赞信息到Redis的set集合
            if (result) {
                stringRedisTemplate.opsForSet().add(key, userId.toString());
            }
            return Result.success("点赞成功", null);
        } else {
            // 如果已点赞，则取消点赞
            // 数据库点赞数-1
            boolean result = update().setSql("liked = liked - 1").eq("id", id).update();
            // 把用户点赞信息从Redis的set集合中移除
            if (result) {
                stringRedisTemplate.opsForSet().remove(key, userId.toString());
            }
            return Result.success("取消点赞成功", null);
        }
    }
}
