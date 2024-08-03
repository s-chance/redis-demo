package org.entropy.smslogin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.entropy.smslogin.dto.LoginFormDTO;
import org.entropy.smslogin.pojo.Result;
import org.entropy.smslogin.pojo.User;
import org.entropy.smslogin.service.UserService;
import org.entropy.smslogin.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 发送短信验证码
     */
    @Operation(summary = "发送短信验证码", description = "根据手机号发送验证码")
    @Parameter(name = "phone", example = "12345678999", description = "手机号", required = true, in = ParameterIn.QUERY)
    @PostMapping("/code")
    public Result<?> code(@RequestParam("phone") String phone) {
        return userService.sendCode(phone);
    }

    /**
     * 登录
     */
    @Operation(summary = "登录", description = "通过验证码登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginFormDTO loginFormDTO) {
        return userService.login(loginFormDTO);
    }

    @Operation(summary = "关于我", description = "登录后查看个人信息")
    @Parameter(name = "Authorization", example = "d962b6baf1d041599b563ef2ce3da49c", description = "token", required = true, in = ParameterIn.HEADER)
    @GetMapping("/me")
    public Result<User> me() {
        User user = UserHolder.getUser();
        return Result.success(user);
    }
}
