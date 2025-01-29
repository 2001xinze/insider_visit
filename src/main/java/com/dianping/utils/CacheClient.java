package com.dianping.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dianping.mapper.ShopMapper;
import com.dianping.pojo.RedisData;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.dianping.pojo.Constant.CACHE_NULL_TTL;
import static com.dianping.pojo.Constant.LOCK_SHOP_KEY;

@Slf4j
@Component
public class CacheClient {

    @Resource
    private ShopMapper shopMapper;

    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set (String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicalExpire (String key, Object value, Long time, TimeUnit timeUnit) {
        // set the logical expiry time
        RedisData redisData = new RedisData();
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        redisData.setData(value);

        // write the object to redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R, ID> R queryWithPassThrough (String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        // 1. get info from redis by id
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. check if the shop exists
        if (StrUtil.isNotBlank(json)) {
            // 3. exists, and return
            return JSONUtil.toBean(json, type);
        }
        //check if the shop null ;null说明未命中，""说明数据库里也没有shop值
        if (json == "") { //要么是null,要么是""
            return null;
        }

        // 4. not exists, query the database by id
        R r = dbFallback.apply(id);
        // 5. if not exists, return false
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 6. the shop exists, save it to redis
        this.set(key, r, time, timeUnit);

        return r;
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    public <R, ID> R queryWithLogicExpireTime (String keyPrefix, ID id, Class <R> type, Function<ID, R> bdFallback, Long time, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        // 1. get info from redis by id
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. check if the shop exists
        if (StrUtil.isBlank(json)) {
            // 3. not exists, and return
            return null;
        }

        // 4. exists, deserializable the json to object
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();

        // 5.check if overtime
        if (expireTime.isAfter(LocalDateTime.now())) {
            // not overdue, return the shop info
            return r;
        }

        // 6.overdue already, rebuild the cache
        // 6.1 get the Mutex
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);

        // check if you successfully get the mutex
        if (isLock) {
            CACHE_REBUILD_EXECUTOR.submit( () -> {
                try {
                    // query the database
                    R r1 = bdFallback.apply(id);
                    // write the data to redis
                    this.setWithLogicalExpire(key, r1, time, timeUnit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unlock(lockKey);
                }
            });
        }
        return r;
    }

    public boolean tryLock (String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    public void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
