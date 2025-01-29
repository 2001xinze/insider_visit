package com.dianping.mapper;

import com.dianping.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select * from dianping.tb_user where phone = #{phone}")
    User getUserByPhone(String phone);

    @Select("select * from tb_user where id = #{userId}")
    User getById(Long userId);
}
