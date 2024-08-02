package org.entropy.smslogin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.entropy.smslogin.pojo.Result;
import org.entropy.smslogin.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
