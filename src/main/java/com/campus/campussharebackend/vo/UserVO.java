package com.campus.campussharebackend.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String studentId;
    private String name;
    private String phone;
    private String email;
    private String avatar;
    private String roleName;    // 角色名称（而非roleId）
}
