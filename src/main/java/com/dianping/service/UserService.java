package com.dianping.service;

import com.dianping.pojo.LoginFormDTO;
import com.dianping.pojo.Result;
import com.dianping.pojo.User;
import jakarta.servlet.http.HttpSession;

public interface UserService {
    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginFormDTO, HttpSession session);

    Result sign();

    Result signCount();

    User getById(Long userId);

    Result queryUserById(Long id);
}
