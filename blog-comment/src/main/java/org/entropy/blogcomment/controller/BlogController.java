package org.entropy.blogcomment.controller;

import org.entropy.blogcomment.pojo.Blog;
import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.service.BlogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blog")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @PostMapping
    public Result<Long> saveBlog(@RequestBody Blog blog) {
        long userId = 1L;
        blog.setUserId(userId);
        blogService.save(blog);
        return Result.success("笔记发布成功", blog.getId());
    }

    @GetMapping("/{id}")
    public Result<?> queryBlogById(@PathVariable("id") Long id) {
        return blogService.queryBlogById(id);
    }

    @PutMapping("/like/{id}")
    public Result<Void> likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }
}
