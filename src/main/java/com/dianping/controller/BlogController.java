package com.dianping.controller;

import com.dianping.pojo.Blog;
import com.dianping.pojo.Result;
import com.dianping.service.BlogService;
import com.dianping.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    @GetMapping("/{id}")
    public Result queryById (@PathVariable("id") Long id) {
        return blogService.queryById(id);
    }

    @PutMapping("/likes/{id}")
    public Result likeBlog (@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    @GetMapping("/hot")
    public Result queryHotBlog (@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    @GetMapping("/likes/{id}")
    public Result queryBlogLikes (@PathVariable("id") Long id) {
        return blogService.queryBlogLikes(id);
    }

    @GetMapping("/of/me")
    public Result queryMyBlog (@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // get the user
        Long userId = UserHolder.getUser().getId();
        return blogService.queryUserBlog(userId, current);
    }

    @GetMapping("/of/user")
    public Result queryUserBlog (@RequestParam(value = "current", defaultValue = "1") Integer current, @RequestParam("id") Long userId) {
        return blogService.queryUserBlog(userId, current);
    }

    @GetMapping("/of/follow")
    public Result queryBlogOfFollow (@RequestParam("lastId") Long max, @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return blogService.queryBlogOfFollow(max, offset);
    }
}
