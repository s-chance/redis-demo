package org.entropy.blogcomment.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.mapper.BlogMapper;
import org.entropy.blogcomment.pojo.Blog;
import org.springframework.stereotype.Service;

@Service
public class BlogService extends ServiceImpl<BlogMapper, Blog> {
}
