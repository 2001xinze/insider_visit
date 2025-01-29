package com.dianping.service;

import com.dianping.pojo.Blog;
import com.dianping.pojo.Result;

public interface BlogService {
    Result saveBlog(Blog blog);

    Result queryById(Long id);

    Result likeBlog(Long id);

    Result queryHotBlog(Integer current);

    Result queryBlogLikes(Long id);

    Result queryUserBlog(Long userId, Integer current);

    Result queryBlogOfFollow(Long max, Integer offset);
}
