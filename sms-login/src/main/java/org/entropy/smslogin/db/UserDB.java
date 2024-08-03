package org.entropy.smslogin.db;

import cn.hutool.core.lang.UUID;
import org.entropy.smslogin.pojo.User;

import java.util.HashMap;
import java.util.Map;

public class UserDB {
    private final Map<String, User> map = new HashMap<>() {{
        put("12345678999", new User(1L, "李四"));
        put("12567778877", new User(2L, "王五"));
    }};

    public User getUserByPhone(String phone) {
        return map.get(phone);
    }

    public User saveUser(String phone) {
        Long id = Long.valueOf(UUID.randomUUID().toString());
        map.put(phone, new User(id, "user_" + phone));
        return map.get(phone);
    }
}
