package com.campus.campussharebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.campus.campussharebackend.entity.Permission;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {

    // 根据角色ID查询该角色拥有的所有权限关键字
    @Select("SELECT p.permission_key " +
            "FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId}")
    List<String> selectPermissionKeysByRoleId(Integer roleId);
}