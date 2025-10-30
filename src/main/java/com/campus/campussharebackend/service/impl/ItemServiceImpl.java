package com.campus.campussharebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.campussharebackend.dto.ItemPublishDTO;
import com.campus.campussharebackend.dto.ItemQueryDTO;
import com.campus.campussharebackend.entity.Item;
import com.campus.campussharebackend.entity.ItemImage;
import com.campus.campussharebackend.entity.User;
import com.campus.campussharebackend.exception.BusinessException;
import com.campus.campussharebackend.mapper.ItemImageMapper;
import com.campus.campussharebackend.mapper.ItemMapper;
import com.campus.campussharebackend.mapper.ItemTypeMapper;
import com.campus.campussharebackend.mapper.UserMapper;
import com.campus.campussharebackend.service.ItemService;
import com.campus.campussharebackend.utils.FileUtils;
import com.campus.campussharebackend.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private ItemImageMapper itemImageMapper;

    @Resource
    private ItemTypeMapper itemTypeMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private FileUtils fileUtils;  // 工具类：处理文件上传

    // 物品状态常量
    private static final Integer STATUS_PENDING_AUDIT = 0; // 待审核
    private static final Integer STATUS_AVAILABLE = 1;      // 可借用
    private static final Integer STATUS_BORROWED = 2;       // 已借出
    private static final Integer STATUS_OFFLINE = 3;        // 已下架


    @Override
    @Transactional
    public Long publishItem(ItemPublishDTO publishDTO, Long userId) {
        // 1. 校验参数
        if (publishDTO.getImages() == null || publishDTO.getImages().isEmpty()) {
            throw new BusinessException("至少上传一张图片");
        }

        // 2. 保存物品基本信息（状态默认为"待审核"）
        Item item = new Item();
        BeanUtils.copyProperties(publishDTO, item);
        item.setUserId(userId);
        item.setStatus(STATUS_PENDING_AUDIT); // 发布后待审核
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        item.setIsDelete(0);
        itemMapper.insert(item);
        Long itemId = item.getId(); // 获取自增ID


        // 3. 处理图片上传并保存图片信息
        List<MultipartFile> images = publishDTO.getImages();
        Integer mainIndex = publishDTO.getMainImageIndex() == null ? 0 : publishDTO.getMainImageIndex();

        if (mainIndex < 0 || mainIndex >= images.size()) {
            throw new BusinessException("主图索引无效");
        }

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            // 上传图片到服务器（返回图片URL）
            String imageUrl = fileUtils.uploadFile(file);
            if (imageUrl == null) {
                throw new BusinessException("图片上传失败");
            }

            // 保存图片信息到数据库
            ItemImage itemImage = new ItemImage();
            itemImage.setItemId(itemId);
            itemImage.setImageUrl(imageUrl);
            // 设置主图（指定索引的图片为主图）
            itemImage.setIsMain(i == mainIndex ? 1 : 0);
            itemImage.setCreateTime(LocalDateTime.now());
            itemImageMapper.insert(itemImage);
        }

        return itemId;
    }


    @Override
    @Transactional
    public void auditItem(Long itemId, Long operatorId) {
        // 1. 校验物品存在且状态为"待审核"
        Item item = itemMapper.selectById(itemId);
        if (item == null || item.getIsDelete() == 1) {
            throw new BusinessException("物品不存在");
        }
        if (!item.getStatus().equals(STATUS_PENDING_AUDIT)) {
            throw new BusinessException("只有待审核的物品可审核");
        }

        // 2. 校验操作人是管理员（简化：实际需调用权限工具类）
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new BusinessException("用户不存在");
        }
        // 关键修正：用role_id判断（2=管理员），而非name字段
        if (operator.getRoleId() != 2) {
            throw new BusinessException("无审核权限（需管理员）");
        }

        // 3. 更新状态为"可借用"
        item.setStatus(STATUS_AVAILABLE);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
    }


    @Override
    @Transactional
    public void offlineItem(Long itemId, Long operatorId) {
        // 1. 校验物品存在
        Item item = itemMapper.selectById(itemId);
        if (item == null || item.getIsDelete() == 1) {
            throw new BusinessException("物品不存在");
        }

        // 2. 校验操作人是物品发布者
        if (!item.getUserId().equals(operatorId)) {
            throw new BusinessException("只能下架自己发布的物品");
        }

        // 3. 更新状态为"已下架"
        item.setStatus(STATUS_OFFLINE);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);
    }


    @Override
    public IPage<ItemListVO> queryItemList(ItemQueryDTO queryDTO) {
        // 1. 构建分页条件
        IPage<Item> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建查询条件（修复：所有字段添加表别名 i.，与 Mapper 中 SQL 一致）
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(queryDTO.getItemTypeId() != null, "i.item_type_id", queryDTO.getItemTypeId());
        queryWrapper.eq(queryDTO.getStatus() != null, "i.status", queryDTO.getStatus());
        queryWrapper.like(queryDTO.getName() != null, "i.name", queryDTO.getName());
        queryWrapper.eq("i.is_delete", 0); // 只查询未删除的物品
        queryWrapper.orderByDesc("i.create_time"); // 按创建时间倒序


        // 3. 分页查询（关联查询分类名称、主图等）
        IPage<ItemListVO> itemPage = itemMapper.selectItemList(page, queryWrapper);

        // 4. 转换状态码为文本（如0→"待审核"）
        itemPage.getRecords().forEach(vo -> {
            vo.setStatusText(convertStatusToText(vo.getStatus()));
        });

        return itemPage;
    }


    @Override
    public ItemDetailVO getItemDetail(Long itemId) {
        // 1. 校验物品存在
        Item item = itemMapper.selectById(itemId);
        if (item == null || item.getIsDelete() == 1) {
            throw new BusinessException("物品不存在");
        }

        // 2. 基础信息封装
        ItemDetailVO detailVO = new ItemDetailVO();
        BeanUtils.copyProperties(item, detailVO);

        // 3. 补充分类名称
        String typeName = itemTypeMapper.selectTypeNameById(item.getItemTypeId());
        detailVO.setTypeName(typeName);

        // 4. 补充图片信息
        List<String> imageUrls = itemImageMapper.selectImageUrlsByItemId(itemId);
        detailVO.setImageUrls(imageUrls);
        detailVO.setMainImageUrl(itemImageMapper.selectMainImageUrl(itemId));

        // 5. 补充发布者信息
        User publisher = userMapper.selectById(item.getUserId());
        detailVO.setPublisherName(publisher.getName());
        detailVO.setPublisherAvatar(publisher.getAvatar());
        detailVO.setUserId(publisher.getId());

        // 6. 补充状态文本
        detailVO.setStatusText(convertStatusToText(item.getStatus()));

        // 7. 补充评价信息（简化：实际需关联comment表查询）
        detailVO.setCommentCount(0); // 临时默认值
        detailVO.setAvgScore(5.0);   // 临时默认值

        return detailVO;
    }


    // 状态码转文本
    private String convertStatusToText(Integer status) {
        switch (status) {
            case 0: return "待审核";
            case 1: return "可借用";
            case 2: return "已借出";
            case 3: return "已下架";
            default: return "未知状态";
        }
    }
    @Override
    public List<ItemStatusCountVO> getItemStatusStats() {
        return itemMapper.selectItemStatusStats();
    }

    @Override
    public List<CategoryItemCountVO> getCategoryItemStats() {
        return itemMapper.selectCategoryItemStats();
    }
    @Override
    public StatsOverviewVO getStatsOverview() {
        StatsOverviewVO overview = new StatsOverviewVO();
        // 物品状态统计
        overview.setItemStatusStats(getItemStatusStats());
        // 分类物品统计
        overview.setCategoryItemStats(getCategoryItemStats());
        // 评价统计（若有评价模块，补充此处）
        overview.setCommentStats(null); // 暂空，后续可扩展
        return overview;
    }
}