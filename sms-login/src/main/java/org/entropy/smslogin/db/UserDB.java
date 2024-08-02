package org.entropy.smslogin.db;

import java.util.HashMap;
import java.util.Map;

public class UserDB {
    private final Map<String, String> map = new HashMap<>() {{
        put("12345678999", "李四");
        put("12567778877", "王五");
    }};

    public String getUserByPhone(String phone) {
        return map.get(phone);
    }

    public String saveUser(String phone) {
        map.put(phone, "user_" + phone);
        return map.get(phone);
    }
}
