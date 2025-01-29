package com.dianping.mapper;

import com.dianping.pojo.Follow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FollowerMapper {

    @Select("select count(*) from tb_follow where user_id = #{userId} and follow_user_id = #{followUserId}")
    Integer count(Long userId, Long followUserId);

    boolean save(Follow follow);

    @Delete("delete from tb_follow where user_id = #{userId} and follow_user_id = #{followUserId}")
    boolean remove(Long userId, Long followUserId);

    @Select("select * from tb_follow where follow_user_id = #{followUserId}")
    List<Follow> getFans(Long followUserId);
}
