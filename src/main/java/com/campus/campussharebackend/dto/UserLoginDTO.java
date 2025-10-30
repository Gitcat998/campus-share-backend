package com.campus.campussharebackend.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class UserLoginDTO {
    @NotBlank(message = "账号不能为空")
    private String account;     // 账号（用户名或学号）

    @NotBlank(message = "密码不能为空")
    private String password;    // 明文密码
}
