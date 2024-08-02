package org.entropy.smslogin.service;

import cn.hutool.core.util.RandomUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.entropy.smslogin.pojo.Result;

@Slf4j
public class UserService {

    public Result<?> sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if (phone.length() != 11) {
            // 2.不符合，返回错误信息
            return Result.failure("手机号不正确");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.保存验证码到session
        session.setAttribute("code", code);
        // 5.发送验证码
        log.debug("发送短信验证码：{}", code);
        return Result.success(null);
    }
}
