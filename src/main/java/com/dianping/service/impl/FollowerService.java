package com.dianping.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.dianping.mapper.FollowerMapper;
import com.dianping.pojo.Follow;
import com.dianping.pojo.Result;
import com.dianping.pojo.UserDTO;
import com.dianping.service.UserService;
import com.dianping.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowerService implements com.dianping.service.FollowerService {

    @Resource
    private FollowerMapper followerMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserService userService;

    @Override
    public Result isFollow(Long followUserId) {
        // get the login user
        Long userId = UserHolder.getUser().getId();
        // check if the login user follows
        Integer count = followerMapper.count(userId, followUserId);
        return Result.ok(count > 0);
    }

    @Override
    public Result followCommons(Long id) {
        // get the login user
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        // get the intersection
        String key2 = "follows:" + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        if (intersect == null || intersect.isEmpty()) {
            // have no intersection
            return Result.ok(Collections.emptyList());
        }
        // parse the id collection
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        // query the user
        List<UserDTO> users = ids
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(users);
    }

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        // get the login user
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        // check if the user follows
        if (isFollow) {
            // if not follow
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = followerMapper.save(follow);
            if (isSuccess) {
                // put the user follows to redis
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // if follows
            boolean isSuccess = followerMapper.remove(userId, followUserId);
            if (isSuccess) {
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.ok();
    }
}
