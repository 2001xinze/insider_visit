package com.dianping.service.impl;

import cn.hutool.json.JSONUtil;
import com.dianping.mapper.ShopMapper;
import com.dianping.pojo.*;
import com.dianping.utils.CacheClient;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.dianping.pojo.Constant.CACHE_SHOP_KEY;
import static com.dianping.pojo.Constant.CACHE_SHOP_TTL;
import static com.dianping.pojo.SystemConstants.MAX_PAGE_SIZE;

@Slf4j
@Service
public class ShopService implements com.dianping.service.ShopService {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    CacheClient cacheClient;

    @Resource
    private ShopMapper shopMapper;

    @Override
    public Result queryById(Long id) {
//        // 缓存穿透
//        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, id2 -> shopMapper.getShopById(id2), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 缓存击穿
        Shop shop = cacheClient.queryWithLogicExpireTime(CACHE_SHOP_KEY, id, Shop.class, id2 -> shopMapper.getShopById(id2), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("shop id cannot be null");
        }

        // 1. update the database
        shopMapper.updateById(shop);
        // 2. delete the cache in redis
        stringRedisTemplate.delete(Constant.CACHE_SHOP_ID + id);
        return Result.ok();
    }

    @Override
    public void save(Shop shop) {
        shopMapper.save(shop);
    }

    @Override
    public void saveShopToRedis (Long id, Long expireSeconds) {
        // 1.query the data of shop
        Shop shop = shopMapper.getShopById(id);
        // 2.encapsulate the logical expiretime
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // 3.write it to redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    @Override
    public List<Shop> list() {
        List<Shop> shops = shopMapper.list();
        return shops;
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        // check if it needs x, y
        if (x == null || y == null) {
            // don't need to query based on coordinate
            List<Shop> records = query(typeId, current);
            return Result.ok(records);
        }

        // Paginated queries parameters
        int from = (current - 1) * MAX_PAGE_SIZE;
        int end = current * MAX_PAGE_SIZE;

        // query the redis, sorted by distance. result: shopId, distance
        String key = "shop:geo:" + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                key,
                GeoReference.fromCoordinate(x, y),
                new Distance(5000),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
        );
        // parse id
        if (results == null) {
            return Result.ok();
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from) {
            // have no more next page
            return Result.ok();
        }
        // get from to end
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result -> {
            // shop id
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            // distance
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr, distance);
        });
        // query the shop by id
        List<Shop> shops = new ArrayList<>(list.size());
        for (Long id : ids) {
            Shop shop = shopMapper.getShopById(id);
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
            shops.add(shop);
        }
        // return
        return Result.ok(shops);
    }

    public List<Shop> query (Integer typeId, Integer current) {
        //
        PageHelper.startPage(current, MAX_PAGE_SIZE);

        // query
        List<Shop> blogList = shopMapper.shopList(Long.valueOf(typeId));
        Page<Shop> shops = (Page<Shop>) blogList;
        List<Shop> records = shops.getResult();

        return records;
    }


//    public Shop queryWithMutex (Long id) {
//        String key = constant.CACHE_SHOP_ID + id;
//        // 1. get info from redis by id
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        // 2. check if the shop exists
//        if (StrUtil.isNotBlank(shopJson)) {
//            // 3. exists, and return
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//        //check if the shop null ;null说明未命中，""说明数据库里也没有shop值
//        if (shopJson == "") { //要么是null,要么是""
//            return null;
//        }
//
//        // 4. not exists, query the database by id
//        Shop shop = shopMapper.getShopById(id);
//        // 5. if not exists, return false
//        if (shop == null) {
//            stringRedisTemplate.opsForValue().set(key, "", 3L, TimeUnit.MINUTES);
//
//            return null;
//        }
//        // 6. the shop exists, save it to redis
//        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), 30L, TimeUnit.MINUTES);
//
//        return shop;
//    }
}
