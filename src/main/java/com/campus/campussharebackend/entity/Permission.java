package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("permission")  // 权限表
public class Permission {
    @TableId(type = IdType.AUTO)
    private Integer id;             // 权限ID
    private String permissionKey;   // 权限关键字（如"item:audit"、"item:publish"）
    private String description;     // 权限描述（如"物品审核权限"、"发布物品权限"）
}
