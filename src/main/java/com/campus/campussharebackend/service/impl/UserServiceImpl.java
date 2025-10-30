package com.campus.campussharebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.campussharebackend.dto.UserLoginDTO;
import com.campus.campussharebackend.dto.UserRegisterDTO;
import com.campus.campussharebackend.dto.UserUpdateDTO;
import com.campus.campussharebackend.entity.Role;
import com.campus.campussharebackend.entity.User;
import com.campus.campussharebackend.exception.BusinessException;
import com.campus.campussharebackend.mapper.PermissionMapper;
import com.campus.campussharebackend.mapper.RoleMapper;
import com.campus.campussharebackend.mapper.UserMapper;
import com.campus.campussharebackend.service.UserService;
import com.campus.campussharebackend.utils.JwtUtils;
import com.campus.campussharebackend.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;  // 密码加密器

    @Resource
    private JwtUtils jwtUtils;  // JWT工具类
    @Resource
    private PermissionMapper permissionMapper;
    // 默认角色：学生（假设role_id=1）
    private static final Integer DEFAULT_ROLE_ID = 1;
    // 管理员角色ID（假设role_id=2）
    private static final Integer ADMIN_ROLE_ID = 2;

    @Override
    @Transactional
    public void register(UserRegisterDTO registerDTO) {
        // 1. 校验用户名唯一性
        int usernameCount = userMapper.countByUsername(registerDTO.getUsername());
        if (usernameCount > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 校验学号唯一性
        int studentIdCount = userMapper.countByStudentId(registerDTO.getStudentId());
        if (studentIdCount > 0) {
            throw new BusinessException("学号已被注册");
        }

        // 3. 密码加密（BCrypt）
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());

        // 4. 构建User对象并保存
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(encodedPassword);  // 存储加密后的密码
        user.setRoleId(DEFAULT_ROLE_ID);    // 默认学生角色
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
    }

    @Override
    public Map<String, Object> login(UserLoginDTO loginDTO) {
        // 1. 查询用户（支持用户名或学号登录）
        User user = userMapper.selectByUsername(loginDTO.getAccount());
        if (user == null) {
            user = userMapper.selectByStudentId(loginDTO.getAccount());
        }
        if (user == null) {
            throw new BusinessException("账号不存在");
        }

        // 2. 校验密码（BCrypt解密校验）
        boolean passwordMatch = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        if (!passwordMatch) {
            throw new BusinessException("密码错误");
        }

        // 3. 关键：通过用户的roleId查询权限列表（从role_permission表）
        List<String> permissions = permissionMapper.selectPermissionKeysByRoleId(user.getRoleId());
        // 打印权限列表用于调试（确认是否包含"PERMISSION_item:publish"）
        System.out.println("用户[" + user.getUsername() + "]的权限列表: " + permissions);

        // 若权限为空，说明该角色未关联任何权限，需检查数据库配置
        if (permissions == null || permissions.isEmpty()) {
            throw new BusinessException("当前用户无任何操作权限");
        }

        // 4. 新增：查询角色名称（用于Token生成，避免过滤器空指针）
        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            throw new BusinessException("角色信息异常");
        }
        String roleName = role.getRoleName(); // 获取角色名（如"学生"、"管理员"）
        System.out.println("用户[" + user.getUsername() + "]的角色名称: " + roleName);

        // 5. 生成JWT Token（修改：传入userId、roleName、permissions，确保Token包含角色名）
        String token = jwtUtils.generateToken(user.getId(), roleName, permissions);
        System.out.println("生成的Token（含角色和权限）: " + token);

        // 6. 构建返回结果（角色名称仅用于前端展示）
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRoleName(roleName);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", userVO);
        return result;
    }

    /**
     * 将中文角色名称转换为英文角色名称
     */
    private String convertChineseRoleToEnglish(String chineseRole) {
        switch (chineseRole) {
            case "管理员":
                return "ADMIN";
            case "学生":
                return "STUDENT";
            default:
                return "USER";
        }
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 查询角色名称
        String roleName = roleMapper.selectRoleNameById(user.getRoleId());

        // 3. 转换为VO返回
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRoleName(roleName);

        return userVO;
    }

    @Override
    @Transactional
    public void updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        // 1. 校验用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 更新允许修改的字段
        if (updateDTO.getName() != null) {
            user.setName(updateDTO.getName());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getEmail() != null) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updateRole(Long userId, Integer newRoleId, Long operatorId) {
        // 1. 校验操作人是否为管理员（operatorId是当前登录用户ID）
        User operator = userMapper.selectById(operatorId);
        if (operator == null || !ADMIN_ROLE_ID.equals(operator.getRoleId())) {
            throw new BusinessException("无权限修改角色（需管理员权限）");
        }

        // 2. 校验目标用户存在
        User targetUser = userMapper.selectById(userId);
        if (targetUser == null) {
            throw new BusinessException("目标用户不存在");
        }

        // 3. 校验新角色ID有效
        Role role = roleMapper.selectById(newRoleId);
        if (role == null) {
            throw new BusinessException("角色ID无效");
        }

        // 4. 更新角色
        targetUser.setRoleId(newRoleId);
        targetUser.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(targetUser);
    }
}