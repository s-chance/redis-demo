package org.entropy.blogcomment.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.dto.ScrollDTO;
import org.entropy.blogcomment.dto.UserDTO;
import org.entropy.blogcomment.mapper.BlogMapper;
import org.entropy.blogcomment.pojo.Blog;
import org.entropy.blogcomment.pojo.Follow;
import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.pojo.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class BlogService extends ServiceImpl<BlogMapper, Blog> {
    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    private final FollowService followService;
    private final BlogMapper blogMapper;

    public BlogService(UserService userService, StringRedisTemplate stringRedisTemplate, FollowService followService, BlogMapper blogMapper) {
        this.userService = userService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.followService = followService;
        this.blogMapper = blogMapper;
    }

    public Result<?> saveBlog(Blog blog) {
        long userId = 1L;
        blog.setUserId(userId);
        boolean result = save(blog);
        if (!result) {
            return Result.failure("笔记发布失败");
        }
        // 查询所有关注者
        List<Follow> follows = followService.query().eq("follow_user_id", userId).list();
        follows.forEach(follow -> {
            // 获取关注者id
            Long userId1 = follow.getUserId();
            // 推送到redis
            String key = "feed:" + userId1;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        });
        return Result.success("笔记发布成功", blog.getId());
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
        isBlogLiked(blog);
        return Result.success("查询成功", blog);
    }

    private void isBlogLiked(Blog blog) {
        // 判断当前用户是否已经点赞，假设当前用户id为1
        Long userId = 1L;
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

    public Result<?> queryBlogByFollow(Long max, Integer offset) {
        // 获取当前用户，假设用户id为1
        Long userId = 1L;
        // 查询收件箱
        String key = "feed:" + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 3);
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.success("暂无新内容", Collections.emptyList());
        }
        // 解析数据
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = max;
        int offset1 = offset;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            if (tuple.getValue() != null && tuple.getScore() != null) {
                // 获取id
                ids.add(Long.valueOf(tuple.getValue()));
                // 获取时间戳
                long time = tuple.getScore().longValue();
                if (time == minTime) {
                    offset1++;
                } else {
                    minTime = time;
                    offset1 = 1;
                }
            }
        }
        // 根据id查询blog
        List<Blog> blogs = blogMapper.queryBlogByFollow(ids);

        blogs.forEach(blog -> {
            User user = userService.getById(blog.getUserId());
            blog.setName(user.getNickname());
            blog.setAvatar(user.getAvatar());

            // 查询blog是否被当前用户点赞
            isBlogLiked(blog);
        });

        // 封装并返回
        ScrollDTO scrollDTO = new ScrollDTO();
        scrollDTO.setList(blogs);
        scrollDTO.setOffset(offset1);
        scrollDTO.setMinTime(minTime);

        return Result.success("查询成功", scrollDTO);
    }
}
