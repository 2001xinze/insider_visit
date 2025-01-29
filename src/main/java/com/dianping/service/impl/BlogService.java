package com.dianping.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.dianping.mapper.BlogMapper;
import com.dianping.mapper.FollowerMapper;
import com.dianping.pojo.*;
import com.dianping.service.UserService;
import com.dianping.utils.UserHolder;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dianping.pojo.Constant.BLOG_LIKED_KEY;
import static com.dianping.pojo.Constant.FEED_KEY;

@Service
public class BlogService implements com.dianping.service.BlogService {

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private FollowerMapper followerMapper;

    @Override
    public Result saveBlog(Blog blog) {
        // get the user
        UserDTO userDTO = UserHolder.getUser();
        blog.setUserId(userDTO.getId());
        // save the blog info
        boolean isSuccess = blogMapper.saveBlog(blog);
        if (!isSuccess) {
            return Result.fail("insert blog unsuccessfully");
        }
        // query all the fans of the user: select * from tb_follow where follow_user_id = ?
        Long followUserId = userDTO.getId();
        List<Follow> follows = followerMapper.getFans(followUserId);
        // send the blog to the fans
        for (Follow follow : follows) {
            // get the fan's id
            Long userId = follow.getUserId();
            // send the blog to redis
            String key = FEED_KEY + userId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
        // return
        return Result.ok(blog.getId());
    }

    @Override
    public Result queryById(Long id) {
        Blog blog = blogMapper.getById(id);
        if (blog == null) {
            return Result.fail("the note doesn't exist");
        }

        isBlogLiked(blog);
        return Result.ok(blog);
    }

    private void isBlogLiked(Blog blog) {
        // get the user
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return;
        }
        Long userId = UserHolder.getUser().getId();
        // check if the user has liked the block
        String key = BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    @Override
    public Result likeBlog(Long id) {
        // get the user
        Long userId = UserHolder.getUser().getId();
        // check if the user has liked the block
        String key = BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            // if it has not liked
            // database +1
            boolean isSuccess = blogMapper.updatePlus(id);
            // save the info to redis  zadd key value score
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // if hase liked
            // database -1
            boolean isSuccess = blogMapper.updateMinus(id);
            // delete the info to redis
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }

        return Result.ok();
    }

    @Override
    public Result queryHotBlog(Integer current) {
        //
        PageHelper.startPage(current, SystemConstants.MAX_PAGE_SIZE);

        // query
        List<Blog> blogList = blogMapper.list();
        Page<Blog> blogs = (Page<Blog>) blogList;
        List<Blog> records = blogs.getResult();

        // query the blog
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });

        return Result.ok(records);
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        // 1.query the top5 zrange key 0 4
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 2.Resolves the id of the user
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        // 3.query the user based on the id
        List<User> users = new ArrayList<>();
        for (Long num : ids) {
            users.add(userService.getById(num));
        }
        List<UserDTO> userDTOS = users
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        // return
        return Result.ok(userDTOS);
    }

    @Override
    public Result queryUserBlog(Long userId, Integer current) {
        //
        PageHelper.startPage(current, SystemConstants.MAX_PAGE_SIZE);

        // query
        List<Blog> blogList = blogMapper.userList();
        Page<Blog> blogs = (Page<Blog>) blogList;
        List<Blog> records = blogs.getResult();

        return Result.ok(records);
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        // 1.get the user
        Long userId = UserHolder.getUser().getId();
        String key = FEED_KEY + userId;
        // 2.check the inbox: ZRANGEBYSCORE KEY min max WITHSCORES LIMIT offset count
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 3);
        if (typedTuples.isEmpty() || typedTuples == null) {
            return Result.ok();
        }
        // 3.parse the data:blogId, score(timeStamp), offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            // get id
            ids.add(Long.valueOf(tuple.getValue()));
            // get score(timestamp)
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            } else {
                minTime = minTime;
                os = 1;
            }
        }
        // 4.query blog based on blogId
        List<Blog> blogs = new ArrayList<>(typedTuples.size());
        for (Long id : ids) {
            Blog blog = blogMapper.getById(id);
            blogs.add(blog);

            // query user
            queryBlogUser(blog);
            // query if the blog is liked
            isBlogLiked(blog);
        }
        // Encapsulation results
        ScrollResult result = new ScrollResult();
        result.setList(blogs);
        result.setOffset(os);
        result.setMinTime(minTime);
        return Result.ok(result);
    }
}
