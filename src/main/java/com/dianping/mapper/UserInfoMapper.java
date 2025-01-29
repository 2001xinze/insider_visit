package com.dianping.mapper;

import com.dianping.pojo.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserInfoMapper {
    @Select("select * from tb_user_info where user_id = #{id}")
    UserInfo getUserInfoById(Long userId);
}
