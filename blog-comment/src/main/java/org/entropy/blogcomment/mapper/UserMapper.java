package org.entropy.blogcomment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.entropy.blogcomment.pojo.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    List<User> queryUserList(@Param("ids") List<Long> ids);
}
