package com.campus.campussharebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.campus.campussharebackend.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    // 校验用户名唯一性
    @Select("select count(*) from user where username = #{username}")
    int countByUsername(String username);

    // 校验学号唯一性
    @Select("select count(*) from user where student_id = #{studentId}")
    int countByStudentId(String studentId);

    // 根据用户名查询用户
    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    // 根据学号查询用户
    @Select("select * from user where student_id = #{studentId}")
    User selectByStudentId(String studentId);
}
