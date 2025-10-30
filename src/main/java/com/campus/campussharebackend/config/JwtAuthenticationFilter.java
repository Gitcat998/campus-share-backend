package com.campus.campussharebackend.config;

import com.campus.campussharebackend.entity.User;
import com.campus.campussharebackend.mapper.PermissionMapper;
import com.campus.campussharebackend.mapper.UserMapper;
import com.campus.campussharebackend.utils.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器：拦截请求并验证Token
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    private UserMapper userMapper;

    @Resource
    private PermissionMapper permissionMapper;
    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("=== JWT Filter 开始 ===");
        String requestUri = request.getRequestURI();
        System.out.println("请求 URI: " + requestUri);

        // 优先放行图片等静态资源（新增：解决图片请求被拦截问题）
        if (requestUri.startsWith("/images/")) {
            System.out.println("图片资源，直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        // 公开接口放行（不变）
        if ("/api/v1/users/login".equals(requestUri) || "/api/v1/users/register".equals(requestUri)) {
            System.out.println("公开接口，直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authorization = request.getHeader("Authorization");
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
                System.out.println("提取到Token: " + token);
            } else {
                System.out.println("未找到有效Token");
            }

            if (token != null && !jwtUtils.isTokenExpired(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                String roleName = jwtUtils.getRoleNameFromToken(token);
                System.out.println("解析Token - 用户ID: " + userId + ", 角色: " + roleName);

                // 1. 查询用户角色ID
                User user = userMapper.selectById(userId);
                Integer roleId = user.getRoleId();

                // 2. 根据角色ID查询权限关键字列表
                List<String> permissions = permissionMapper.selectPermissionKeysByRoleId(roleId);
                System.out.println("用户权限列表: " + permissions); // 确认输出包含PERMISSION_item:publish

                // 3. 构建权限集合（关键修改：添加查询到的权限）
                List<GrantedAuthority> authorities = new ArrayList<>();
                // 将数据库查询的权限添加到authorities中
                for (String permission : permissions) {
                    authorities.add(new SimpleGrantedAuthority(permission)); // 核心：添加权限
                }

                // （可选）如果需要保留角色权限，可添加角色（注意：角色需以ROLE_开头）
                if (!roleName.startsWith("ROLE_")) {
                    roleName = "ROLE_" + roleName;
                }
                authorities.add(new SimpleGrantedAuthority(roleName)); // 添加角色（可选）
                System.out.println("处理后的权限集合: " + authorities); // 确认包含PERMISSION_item:publish

                // 4. 设置认证信息（包含权限）
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities); // 传入填充后的authorities
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("认证设置完成，当前权限: " + authorities); // 此处输出包含目标权限
            } else {
                System.out.println("Token无效或已过期");
            }
        } catch (Exception e) {
            System.out.println("JWT过滤器异常: " + e.getMessage());
            logger.error("无法设置用户认证: " + e);
        }

        System.out.println("=== JWT Filter 结束 ===");
        filterChain.doFilter(request, response);
    }

    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void setPermissionMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }
}