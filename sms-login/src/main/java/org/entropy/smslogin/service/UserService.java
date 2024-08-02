package org.entropy.smslogin.service;

import cn.hutool.core.util.RandomUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.entropy.smslogin.db.UserDB;
import org.entropy.smslogin.dto.LoginFormDTO;
import org.entropy.smslogin.pojo.Result;

import java.util.Objects;

@Slf4j
public class UserService {

    private final UserDB userDB = new UserDB();

    public Result<?> sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if (phone.length() != 11) {
            // 2.不符合，返回错误信息
            return Result.failure("手机号不正确");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.保存原始手机号和验证码到session
        session.setAttribute("originalPhone", phone);
        session.setAttribute("code", code);
        // 5.发送验证码
        log.debug("发送短信验证码：{}", code);
        return Result.success(null);
    }

    public Result<?> login(LoginFormDTO loginFormDTO, HttpSession session) {
        // 1.校验手机号
        String phone = loginFormDTO.getPhone();
        if (phone.length() != 11) {
            // 不符合，返回错误信息
            return Result.failure("手机号不正确");
        }
        // 防止验证码重用攻击，校验手机号是否为接收验证码的手机号
        String originalPhone = (String) session.getAttribute("originalPhone");
        if (!Objects.equals(originalPhone, phone)) {
            return Result.failure("该手机号与接收验证码的手机号不一致，请确认接收验证码的手机号");
        }

        // 2.校验验证码
        String cacheCode = (String) session.getAttribute("code");
        String code = loginFormDTO.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 3.不一致
            return Result.failure("验证码不一致");
        }
        // 4.一致，根据手机号查询用户
        String userByPhone = userDB.getUserByPhone(phone);
        // 5.判断用户是否存在
        if (userByPhone == null) {
            // 6.不存在，创建新用户并保存
            userByPhone = userDB.saveUser(phone);
        }
        // 7.保存用户信息到session中
        session.setAttribute("loginUser", userByPhone);
        return Result.success(null);
    }
}
