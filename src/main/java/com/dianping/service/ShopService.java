package com.dianping.service;

import com.dianping.pojo.Result;
import com.dianping.pojo.Shop;

import java.util.List;

public interface ShopService {
    Result queryById(Long id);

    Result update(Shop shop);

    void save(Shop shop);

    void saveShopToRedis (Long id, Long expireSeconds);


    List<Shop> list();

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
