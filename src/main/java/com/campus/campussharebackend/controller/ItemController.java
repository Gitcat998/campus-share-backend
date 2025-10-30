package com.campus.campussharebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.campus.campussharebackend.dto.ItemPublishDTO;
import com.campus.campussharebackend.dto.ItemQueryDTO;

import com.campus.campussharebackend.service.ItemService;
import com.campus.campussharebackend.utils.JwtUtils;
import com.campus.campussharebackend.utils.Result;
import com.campus.campussharebackend.vo.ItemDetailVO;
import com.campus.campussharebackend.vo.ItemListVO;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    @Resource
    private ItemService itemService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private JwtUtils jwtUtils; // 用于获取当前登录用户ID


    /**
     * 发布物品（需"item:publish"权限）
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)  // 新增此行
    @PreAuthorize("hasAuthority('PERMISSION_item:publish')")
    public Result<Long> publishItem(@Validated ItemPublishDTO publishDTO) {
        Long userId = getCurrentUserId();
        Long itemId = itemService.publishItem(publishDTO, userId);
        return Result.success(itemId);
    }


    /**
     * 管理员审核物品（需"item:audit"权限）
     */
    @PutMapping("/{itemId}/audit")
    @PreAuthorize("hasAuthority('item:audit')")
    public Result auditItem(@PathVariable Long itemId) {
        Long adminId = getCurrentUserId(); // 管理员ID
        itemService.auditItem(itemId, adminId);
        return Result.success("审核通过，物品状态更新为可借用");
    }


    /**
     * 发布者下架物品
     */
    @PutMapping("/{itemId}/offline")
    public Result offlineItem(@PathVariable Long itemId) {
        Long userId = getCurrentUserId(); // 发布者ID
        itemService.offlineItem(itemId, userId);
        return Result.success("物品已下架");
    }


    /**
     * 条件查询物品列表
     */
    @GetMapping
    public Result<IPage<ItemListVO>> queryItemList(ItemQueryDTO queryDTO) {
        IPage<ItemListVO> itemPage = itemService.queryItemList(queryDTO);
        return Result.success(itemPage);
    }


    /**
     * 查询物品详情
     */
    @GetMapping("/{itemId}")
    public Result<ItemDetailVO> getItemDetail(@PathVariable Long itemId) {
        ItemDetailVO detailVO = itemService.getItemDetail(itemId);
        return Result.success(detailVO);
    }


    // 从Token获取当前用户ID（复用之前的方法）
    private Long getCurrentUserId() {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未获取到有效Token");
        }
        // 去除"Bearer "前缀（注意空格）
        String actualToken = token.substring(7);
        return jwtUtils.getUserIdFromToken(actualToken);
    }
}