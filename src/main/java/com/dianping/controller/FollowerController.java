package com.dianping.controller;

import com.dianping.pojo.Result;
import com.dianping.service.FollowerService;
import com.dianping.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
public class FollowerController {
    @Resource
    private FollowerService followerService;

    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        return followerService.isFollow(followUserId);
    }

    @PutMapping("/{id}/{isFollow}")
    public Result follow (@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {
        return followerService.follow(followUserId, isFollow);
    }

    @GetMapping("/common/{id}")
    public Result followCommon(@PathVariable("id") Long id) {
        return followerService.followCommons(id);
    }
}
