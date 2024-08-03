package org.entropy.smslogin.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.entropy.smslogin.pojo.User;
import org.entropy.smslogin.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.entropy.smslogin.constant.RedisConstants.LOGIN_USER_KEY_PREFIX;
import static org.entropy.smslogin.constant.RedisConstants.LOGIN_USER_TTL;

@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }

        // 2.根据token获取redis中的用户
        String tokenKey = LOGIN_USER_KEY_PREFIX + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);

        // 3.判断用户是否存在
        if (userMap.isEmpty()) {
            // 4.直接放行，根据实际路径决定是否由第二个拦截器处理
            return true;
        }

        // 5.将查询到的Hash数据转为对象
        User user = BeanUtil.fillBeanWithMap(userMap, new User(), false);

        // 6.保存用户信息到ThreadLocal
        User userHolder = UserHolder.getUser();
        if (userHolder == null || !userHolder.equals(user)) {
            UserHolder.saveUser(user);
        }

        // 7.刷新 token 缓存
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUser();
    }
}
