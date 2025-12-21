package com.example.bilibili.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Map;

@Mapper
public interface DemoDao {

    @Select("SELECT * FROM t_demo WHERE id = #{id}")
    Map<String, Object> query(Long id);
}

