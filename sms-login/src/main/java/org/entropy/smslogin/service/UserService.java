package org.entropy.smslogin.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entropy.smslogin.db.UserDB;
import org.entropy.smslogin.dto.LoginFormDTO;
import org.entropy.smslogin.pojo.Result;
import org.entropy.smslogin.pojo.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.entropy.smslogin.constant.RedisConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class UserService {

    private final UserDB userDB = new UserDB();

    private final StringRedisTemplate stringRedisTemplate;

    public Result<?> sendCode(String phone) {
        // 1.校验手机号
        if (phone.length() != 11) {
            // 2.不符合，返回错误信息
            return Result.failure("手机号不正确");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.保存验证码到redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY_PREFIX + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 5.发送验证码
        log.debug("发送短信验证码：{}", code);
        return Result.success(null);
    }

    public Result<?> login(LoginFormDTO loginFormDTO) {
        // 1.校验手机号
        String phone = loginFormDTO.getPhone();
        if (phone.length() != 11) {
            // 2.不符合，返回错误信息
            return Result.failure("手机号不正确");
        }

        // 3.从redis获取验证码校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY_PREFIX + phone);
        String code = loginFormDTO.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 不一致
            return Result.failure("验证码不一致");
        }
        // 验证通过后立即删除验证码，避免重复登录
        stringRedisTemplate.delete(LOGIN_CODE_KEY_PREFIX + phone);

        // 4.一致，根据手机号查询用户
        User userByPhone = userDB.getUserByPhone(phone);
        // 5.判断用户是否存在
        if (userByPhone == null) {
            // 6.不存在，创建新用户并保存
            userByPhone = userDB.saveUser(phone);
        }

        // 7.保存用户信息到redis中
        // 7.1.随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);

        // 7.2.将User对象转为HashMap存储
        Map<String, Object> userMap = BeanUtil.beanToMap(userByPhone, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor(
                                (fieldName, fieldValue) -> fieldValue.toString()
                        ));

        // 7.3.存储
        String tokenKey = LOGIN_USER_KEY_PREFIX + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);

        // 7.4.设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8.返回token
        return Result.success(token);
    }
}
