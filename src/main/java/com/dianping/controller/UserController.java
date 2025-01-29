package com.dianping.controller;

import com.dianping.pojo.LoginFormDTO;
import com.dianping.pojo.Result;
import com.dianping.pojo.UserDTO;
import com.dianping.pojo.UserInfo;
import com.dianping.service.UserInfoService;
import com.dianping.service.UserService;
import com.dianping.utils.UserHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private UserInfoService userInfoService;

    /*
    send the code
     */
    @PostMapping("code")
    public Result sendCode (@RequestParam("phone") String phone, HttpSession session, HttpServletRequest request) {
        // send the code and save it
        return userService.sendCode(phone, session);
    }

    /*
    the login function
     */
    @PostMapping("/login")
    public Result login (@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        return userService.login(loginFormDTO, session);
    }

    /*
    logout
     */
    @PostMapping("/logout")
    public Result logout () {
        return Result.fail("function not finished");
    }

    @GetMapping("/me")
    public Result me () {
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }

    @GetMapping("/{id}")
    public Result queryUserById (@PathVariable("id") Long id) {
        return userService.queryUserById(id);
    }
}
