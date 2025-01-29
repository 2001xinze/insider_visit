package com.dianping.service.impl;

import com.dianping.mapper.ShopTypeMapper;
import com.dianping.pojo.ShopType;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopTypeService implements com.dianping.service.ShopTypeService {
    @Resource
    private ShopTypeMapper shopTypeMapper;

    @Override
    public List<ShopType> queryShopType() {
        return shopTypeMapper.queryShopType();
    }
}
