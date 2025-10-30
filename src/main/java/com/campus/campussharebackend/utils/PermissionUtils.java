package com.campus.campussharebackend.utils;


import com.campus.campussharebackend.entity.User;
import com.campus.campussharebackend.mapper.PermissionMapper;
import com.campus.campussharebackend.mapper.UserMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class PermissionUtils {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PermissionMapper permissionMapper;

    // 管理员角色ID（固定为2）
    private static final Integer ADMIN_ROLE_ID = 2;

    /**
     * 检查用户是否拥有指定权限
     * @param userId 用户ID
     * @param permissionKey 权限关键字
     * @return true-有权限，false-无权限
     */
    public boolean hasPermission(Long userId, String permissionKey) {
        // 1. 查询用户信息获取角色ID
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        Integer roleId = user.getRoleId();

        // 2. 管理员默认拥有所有权限，直接返回true
        if (ADMIN_ROLE_ID.equals(roleId)) {
            return true;
        }

        // 3. 非管理员：查询该角色的所有权限关键字，检查是否包含目标权限
        List<String> userPermissions = permissionMapper.selectPermissionKeysByRoleId(roleId);
        return userPermissions != null && userPermissions.contains(permissionKey);
    }
}