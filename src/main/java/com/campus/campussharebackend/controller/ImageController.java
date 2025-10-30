package com.campus.campussharebackend.controller;

import com.campus.campussharebackend.dto.ImageUploadDTO;
import com.campus.campussharebackend.service.ItemImageService;
import com.campus.campussharebackend.utils.JwtUtils;
import com.campus.campussharebackend.utils.Result;
import com.campus.campussharebackend.vo.ItemImageVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

    @Resource
    private ItemImageService itemImageService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private JwtUtils jwtUtils;


    /**
     * 上传物品图片（支持多图，设置主图）
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public Result uploadImages(@Validated ImageUploadDTO dto) {
        itemImageService.uploadImages(dto);
        return Result.success("图片上传成功");
    }


    /**
     * 查询物品的所有图片（用于详情页预览）
     */
    @GetMapping("/item/{itemId}")
    public Result<List<ItemImageVO>> getImagesByItemId(@PathVariable Long itemId) {
        List<ItemImageVO> images = itemImageService.getImagesByItemId(itemId);
        return Result.success(images);
    }


    /**
     * 删除物品图片（仅发布者或管理员）
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("isAuthenticated()")
    public Result deleteImage(@PathVariable Long imageId) {
        Long operatorId = getCurrentUserId();
        itemImageService.deleteImage(imageId, operatorId);
        return Result.success("图片已删除");
    }


    // 工具方法：获取当前用户ID
    private Long getCurrentUserId() {
        return jwtUtils.getUserIdFromToken(getToken());
    }

    // 工具方法：获取Token
    private String getToken() {
        return request.getHeader("Authorization").substring(7);
    }
}