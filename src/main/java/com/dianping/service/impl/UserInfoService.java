package com.dianping.service.impl;

import com.dianping.mapper.UserInfoMapper;
import com.dianping.pojo.UserInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserInfoService implements com.dianping.service.UserInfoService {
    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public UserInfo getById(Long userId) {
        return userInfoMapper.getUserInfoById(userId);
    }
}
