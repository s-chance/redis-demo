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
    public Result<?> saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    @GetMapping("/{id}")
    public Result<?> queryBlogById(@PathVariable("id") Long id) {
        return blogService.queryBlogById(id);
    }

    @PutMapping("/like/{id}")
    public Result<Void> likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    @GetMapping("/likes/{id}")
    public Result<?> queryLikesById(@PathVariable("id") Long id) {
        return blogService.queryLikesById(id);
    }

    @GetMapping("/follow")
    public Result<?> queryBlogByFollow(
            @RequestParam("lastId") Long max,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset
    ) {
        return blogService.queryBlogByFollow(max, offset);
    }
}
