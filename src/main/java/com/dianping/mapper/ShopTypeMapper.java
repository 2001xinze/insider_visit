package com.dianping.mapper;

import com.dianping.pojo.ShopType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShopTypeMapper {

    List<ShopType> queryShopType();
}
