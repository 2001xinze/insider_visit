package com.dianping.service;

import com.dianping.pojo.Result;


public interface FollowerService {
    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);

    Result followCommons(Long id);
}
