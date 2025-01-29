package com.dianping.mapper;

import com.dianping.pojo.Shop;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopMapper {

    @Select("select * from tb_shop where id = #{id}")
    Shop getShopById(Long id);

    void updateById(Shop shop);

    @Insert("")
    void save(Shop shop);

    @Select("select * from tb_shop")
    List<Shop> list();

    @Select("select * from tb_shop where type_id = #{typeId}")
    List<Shop> shopList(Long typeId);
}
