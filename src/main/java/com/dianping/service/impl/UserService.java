package com.dianping.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.dianping.mapper.UserMapper;
import com.dianping.pojo.LoginFormDTO;
import com.dianping.pojo.Result;
import com.dianping.pojo.User;
import com.dianping.pojo.UserDTO;
import com.dianping.utils.RegexUtils;
import com.dianping.utils.UserHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.dianping.constant.Constant.*;

@Slf4j
@Service
public class UserService implements com.dianping.service.UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserMapper userMapper;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. check the form of the phone
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. if the form does not match, return false
            return Result.fail("the form of the phone is wrong");
        }

        // 3.checked, generate the code
        String code = RandomUtil.randomNumbers(6);

        // 4. save the code in Redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 5. send the code
        log.debug("the code: " + code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginFormDTO, HttpSession session) {
        // 1. check the form of the phone
        String phone = loginFormDTO.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. not match, return false
            return Result.fail("the form of the phone is wrong!");
        }

        // 3. get the code from redis and check the code
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginFormDTO.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("the code is wrong");
        }

        // 4. the code matches, query the user by the phone
        User user = userMapper.getUserByPhone(phone);

        // 5. check if the user exists
        if (user == null) {
            // 6. not exists, create a new user
            user = createUserWithPhone(phone);
        }

        // 7. save the user information in redis
        // 7.1 randomly generate token
        String token = UUID.randomUUID().toString(true);
        // 7.2 transfer user to HashMap to store
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 7.3 store
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 7.4 set the expiration of token
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8. return token
        return Result.ok(token);
    }

    @Override
    public Result sign() {
        // get the user
        Long userId = UserHolder.getUser().getId();

        // get the date
        LocalDateTime now = LocalDateTime.now();

        // Concatenate the key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = "sign:" + userId + keySuffix;

        // get which day it is in this month
        int dayOfMonth = now.getDayOfMonth();

        // save to redis: setbit key offset 1/0
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);

        return Result.ok();
    }

    @Override
    public Result signCount() {
        // get the user
        Long userId = UserHolder.getUser().getId();

        // get the date
        LocalDateTime now = LocalDateTime.now();

        // Concatenate the key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = "sign:" + userId + keySuffix;

        // get which day it is in this month
        int dayOfMonth = now.getDayOfMonth();

        // Get all signs for the month up to today, the return value is a decimal number
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty()) {
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return Result.ok(0);
        }
        // iterator
        int count = 0;
        while (true) {
            // let the number do & operator with 1, get the last bit;check if the bit is 0
            if ((num & 1) == 0) {
                // is 0, which means not sigh
                break;
            } else {
                count++;
            }
            // Shift the number to the right by one place and discard the last bit
            num >>>= 1;
        }

        return Result.ok(count);
    }

    @Override
    public User getById(Long userId) {
        return userMapper.getById(userId);
    }

    @Override
    public Result queryUserById(Long userId) {
        // get the user
        User user = userMapper.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.ok(userDTO);
    }

    private User createUserWithPhone (String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        return user;
    }

































}
