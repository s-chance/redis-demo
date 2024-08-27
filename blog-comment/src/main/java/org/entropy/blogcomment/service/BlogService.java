package org.entropy.blogcomment.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.mapper.BlogMapper;
import org.entropy.blogcomment.pojo.Blog;
import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.pojo.User;
import org.springframework.stereotype.Service;

@Service
public class BlogService extends ServiceImpl<BlogMapper, Blog> {
    private final UserService userService;

    public BlogService(UserService userService) {
        this.userService = userService;
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
        return Result.success("查询成功", blog);
    }
}
