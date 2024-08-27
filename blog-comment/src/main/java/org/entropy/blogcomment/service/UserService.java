package org.entropy.blogcomment.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.entropy.blogcomment.mapper.UserMapper;
import org.entropy.blogcomment.pojo.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<User> queryUserList(List<Long> ids) {
        return userMapper.queryUserList(ids);
    }
}
