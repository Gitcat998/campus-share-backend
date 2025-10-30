package com.campus.campussharebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.campussharebackend.dto.ImageUploadDTO;
import com.campus.campussharebackend.entity.ItemImage;
import com.campus.campussharebackend.vo.ItemImageVO;

import java.util.List;

public interface ItemImageService extends IService<ItemImage> {
    // 上传物品图片（支持多图，设置主图）
    void uploadImages(ImageUploadDTO dto);

    // 查询物品的所有图片（用于详情页预览）
    List<ItemImageVO> getImagesByItemId(Long itemId);

    // 删除物品的图片（支持单张删除）
    void deleteImage(Long imageId, Long operatorId);
}