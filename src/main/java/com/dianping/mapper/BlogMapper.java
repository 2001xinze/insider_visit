package com.dianping.mapper;

import com.dianping.pojo.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BlogMapper {

    boolean saveBlog(Blog blog);

    @Select("select * from tb_blog where id = #{id}")
    Blog getById(Long id);

    @Update("update tb_blog set liked = liked + 1 where id = #{id}")
    boolean updatePlus(Long id);
    @Update("update tb_blog set liked = liked - 1 where id = #{id}")
    boolean updateMinus(Long id);

    List<Blog> list();

    List<Blog> userList();
}
