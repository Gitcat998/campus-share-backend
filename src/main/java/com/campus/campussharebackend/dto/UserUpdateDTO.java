package com.campus.campussharebackend.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String name;        // 可修改的姓名
    private String phone;       // 可修改的手机号
    private String email;       // 可修改的邮箱
    private String avatar;      // 可修改的头像URL
}
