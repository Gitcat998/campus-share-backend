package com.campus.campussharebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.campussharebackend.dto.ImageUploadDTO;
import com.campus.campussharebackend.entity.Item;
import com.campus.campussharebackend.entity.ItemImage;
import com.campus.campussharebackend.exception.BusinessException;
import com.campus.campussharebackend.mapper.ItemImageMapper;
import com.campus.campussharebackend.mapper.ItemMapper;
import com.campus.campussharebackend.service.ItemImageService;
import com.campus.campussharebackend.utils.FileUtils;
import com.campus.campussharebackend.vo.ItemImageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemImageServiceImpl extends ServiceImpl<ItemImageMapper, ItemImage> implements ItemImageService {

    @Resource
    private ItemImageMapper itemImageMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private FileUtils fileUtils; // 复用已有FileUtils


    @Override
    @Transactional
    public void uploadImages(ImageUploadDTO dto) {
        // 1. 校验物品存在
        Item item = itemMapper.selectById(dto.getItemId());
        if (item == null || item.getIsDelete() == 1) {
            throw new BusinessException("物品不存在");
        }

        // 2. 校验文件不为空
        if (dto.getFiles() == null || dto.getFiles().isEmpty()) {
            throw new BusinessException("请选择图片文件");
        }

        // 3. 处理主图索引（默认第一张）
        int mainIndex = dto.getMainImageIndex() != null ? dto.getMainImageIndex() : 0;
        if (mainIndex < 0 || mainIndex >= dto.getFiles().size()) {
            throw new BusinessException("主图索引超出范围");
        }

        // 4. 先将该物品所有图片设为非主图（确保主图唯一）
        itemImageMapper.updateAllNonMain(dto.getItemId());

        // 5. 上传图片并保存记录
        List<ItemImage> images = new ArrayList<>();
        for (int i = 0; i < dto.getFiles().size(); i++) {
            MultipartFile file = dto.getFiles().get(i);
            if (file.isEmpty()) continue;

            // 使用已有FileUtils上传，获取完整访问URL
            String imageUrl = fileUtils.uploadFile(file);
            if (imageUrl == null) {
                throw new BusinessException("第" + (i + 1) + "张图片上传失败");
            }

            // 构建图片记录
            ItemImage image = new ItemImage();
            image.setItemId(dto.getItemId());
            image.setImageUrl(imageUrl);
            image.setIsMain(i == mainIndex ? 1 : 0); // 标记主图
            image.setCreateTime(LocalDateTime.now());
            image.setUpdateTime(LocalDateTime.now());
            images.add(image);
        }

        // 批量保存
        this.saveBatch(images);
    }


    @Override
    public List<ItemImageVO> getImagesByItemId(Long itemId) {
        // 校验物品存在
        Item item = itemMapper.selectById(itemId);
        if (item == null || item.getIsDelete() == 1) {
            throw new BusinessException("物品不存在");
        }
        // 查询图片详情（含ID和主图标识）
        return itemImageMapper.selectImagesByItemId(itemId);
    }


    @Override
    @Transactional
    public void deleteImage(Long imageId, Long operatorId) {
        // 1. 校验图片存在
        ItemImage image = itemImageMapper.selectById(imageId);
        if (image == null) {
            throw new BusinessException("图片不存在");
        }

        // 2. 校验权限（物品发布者或管理员）
        Item item = itemMapper.selectById(image.getItemId());
        if (item == null) {
            throw new BusinessException("关联物品不存在");
        }
        // 假设管理员ID为1（实际通过角色判断）
        if (!item.getUserId().equals(operatorId) && !operatorId.equals(1L)) {
            throw new BusinessException("无权限删除该图片");
        }

        // 3. 若删除主图，自动指定新主图
        if (image.getIsMain() == 1) {
            // 查询剩余图片（按上传时间升序，取最早的作为新主图）
            QueryWrapper<ItemImage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("item_id", image.getItemId())
                    .ne("id", imageId)
                    .orderByAsc("create_time");
            List<ItemImage> remaining = itemImageMapper.selectList(queryWrapper);

            if (!remaining.isEmpty()) {
                ItemImage newMain = remaining.get(0);
                newMain.setIsMain(1);
                newMain.setUpdateTime(LocalDateTime.now());
                itemImageMapper.updateById(newMain);
            }
        }

        // 4. 删除图片记录（实际可补充删除本地文件逻辑）
        itemImageMapper.deleteById(imageId);
    }
}