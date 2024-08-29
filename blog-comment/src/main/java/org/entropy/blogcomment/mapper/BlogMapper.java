package org.entropy.blogcomment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.entropy.blogcomment.pojo.Blog;

import java.util.List;

public interface BlogMapper extends BaseMapper<Blog> {
    List<Blog> queryBlogByFollow(@Param("ids") List<Long> ids);
}
