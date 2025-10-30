package com.campus.campussharebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.campussharebackend.dto.UserLoginDTO;
import com.campus.campussharebackend.dto.UserRegisterDTO;
import com.campus.campussharebackend.dto.UserUpdateDTO;
import com.campus.campussharebackend.entity.User;
import com.campus.campussharebackend.vo.UserVO;

import java.util.Map;

public interface UserService extends IService<User> {

    // 用户注册
    void register(UserRegisterDTO registerDTO);

    // 用户登录（返回token和用户信息）
    Map<String, Object> login(UserLoginDTO loginDTO);

    // 查询个人信息
    UserVO getUserInfo(Long userId);

    // 更新个人信息
    void updateUserInfo(Long userId, UserUpdateDTO updateDTO);

    // 管理员修改用户角色
    void updateRole(Long userId, Integer newRoleId, Long operatorId);
}