package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("role")  // 对应数据库role表
public class Role {
    @TableId(type = IdType.AUTO)
    private Integer id;         // 角色ID
    private String roleName;    // 角色名称（如"学生"、"管理员"）
}