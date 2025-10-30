package com.campus.campussharebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.campus.campussharebackend.entity.Role;
import org.apache.ibatis.annotations.Select;

public interface RoleMapper extends BaseMapper<Role> {

    // 根据角色ID查询角色名称
    @Select("select role_name from role where id = #{roleId}")
    String selectRoleNameById(Integer roleId);
}
