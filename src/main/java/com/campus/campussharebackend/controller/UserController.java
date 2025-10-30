package com.campus.campussharebackend.controller;

import com.campus.campussharebackend.dto.UserLoginDTO;
import com.campus.campussharebackend.dto.UserRegisterDTO;
import com.campus.campussharebackend.dto.UserUpdateDTO;
import com.campus.campussharebackend.exception.BusinessException;
import com.campus.campussharebackend.service.UserService;
import com.campus.campussharebackend.utils.JwtUtils;  // 导入JwtUtils
import com.campus.campussharebackend.utils.Result;
import com.campus.campussharebackend.vo.UserVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest request;

    // 注入JwtUtils（解决找不到符号问题）
    @Resource
    private JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@Validated @RequestBody UserLoginDTO loginDTO) {
        Map<String, Object> loginResult = userService.login(loginDTO);
        return Result.success(loginResult);
    }

    /**
     * 查询个人信息（需登录，从Token获取当前用户ID）
     */
    @GetMapping("/info")
    public Result<UserVO>  getUserInfo() {
        Long userId = getCurrentUserId();
        UserVO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 更新个人信息（需登录）
     */
    @PutMapping("/info")
    public Result updateUserInfo(@RequestBody UserUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();
        userService.updateUserInfo(userId, updateDTO);
        return Result.success("信息更新成功");
    }

    /**
     * 管理员修改用户角色（需管理员权限）
     */
    @PutMapping("/{userId}/role")
    public Result updateRole(@PathVariable Long userId, @RequestParam Integer roleId) {
        Long operatorId = getCurrentUserId();
        userService.updateRole(userId, roleId, operatorId);
        return Result.success("角色修改成功");
    }

    // 仅保留一个getCurrentUserId()方法，使用JwtUtils解析Token
    private Long getCurrentUserId() {
        // 1. 从请求头获取Authorization字段
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException("未携带有效的Token");
        }

        // 2. 提取Token（去除"Bearer "前缀）
        String token = authorization.substring(7);

        // 3. 调用JwtUtils解析用户ID
        return jwtUtils.getUserIdFromToken(token);
    }
}