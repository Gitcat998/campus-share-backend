package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")  // 对应数据库user表
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;             // 主键ID
    private String username;     // 用户名（登录账号）
    @TableField("student_id")
    private String studentId;    // 学号（唯一）
    private String password;     // 加密后的密码
    @TableField("real_name")
    private String name;         // 真实姓名
    private String phone;        // 手机号
    private String email;        // 邮箱
    private String avatar;       // 头像URL
    @TableField("role_id")
    private Integer roleId;      // 角色ID（1-学生，2-管理员）
    @TableField("create_time")
    private LocalDateTime createTime;  // 创建时间
    @TableField("update_time")
    private LocalDateTime updateTime;  // 更新时间

    @TableField("is_delete")
    private Integer isDelete;    // 是否删除（0-未删，1-已删，默认0）
}