package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("role_permission")  // 角色-权限关联表（多对多）
public class RolePermission {
    @TableId
    private Integer roleId;       // 角色ID（关联role表）
    @TableId
    private Integer permissionId; // 权限ID（关联permission表）
}