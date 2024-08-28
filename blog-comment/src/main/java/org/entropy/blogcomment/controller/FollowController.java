package org.entropy.blogcomment.controller;

import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.service.FollowService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
public class FollowController {
    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{id}")
    public Result<Void> follow(@PathVariable("id") Long followUerId) {
        return followService.follow(followUerId);
    }

    @GetMapping("/status/{id}")
    public Result<Boolean> followStatus(@PathVariable("id") Long followUerId) {
        return followService.followStatus(followUerId);
    }

    @GetMapping("/common/{id}")
    public Result<?> followCommon(@PathVariable("id") Long id) {
        return followService.followCommon(id);
    }
}
