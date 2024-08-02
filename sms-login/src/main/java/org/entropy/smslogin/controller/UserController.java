package org.entropy.smslogin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.entropy.smslogin.dto.LoginFormDTO;
import org.entropy.smslogin.pojo.Result;
import org.entropy.smslogin.service.UserService;
import org.entropy.smslogin.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {

    private final UserService userService = new UserService();

    /**
     * 发送短信验证码
     */
    @Operation(summary = "发送短信验证码", description = "根据手机号发送验证码")
    @Parameter(name = "phone", example = "12345678999", description = "手机号", required = true, in = ParameterIn.QUERY)
    @PostMapping("/code")
    public Result<?> code(@RequestParam("phone") String phone, HttpSession session) {
        return userService.sendCode(phone, session);
    }

    /**
     * 登录
     */
    @Operation(summary = "登录", description = "通过验证码登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        return userService.login(loginFormDTO, session);
    }

    @Operation(summary = "关于我", description = "登录后查看个人信息")
    @GetMapping("/me")
    public Result<String> me() {
        String user = UserHolder.getUser();
        return Result.success(user);
    }
}
