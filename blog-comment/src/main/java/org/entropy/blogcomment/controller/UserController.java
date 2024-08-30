package org.entropy.blogcomment.controller;

import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sign")
    public Result<Void> sign() {
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result<Integer> signCount() {
        return userService.signCount();
    }
}
