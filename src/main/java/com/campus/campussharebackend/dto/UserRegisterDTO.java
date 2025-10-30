package com.campus.campussharebackend.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class UserRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;    // 用户名

    @NotBlank(message = "学号不能为空")
    private String studentId;   // 学号

    @NotBlank(message = "密码不能为空")
    private String password;    // 明文密码（前端传入）

    private String name;        // 真实姓名（可选）
    private String phone;       // 手机号（可选）
    private String email;       // 邮箱（可选）
}