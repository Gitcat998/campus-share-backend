package com.campus.campussharebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.campussharebackend.dto.ItemPublishDTO;
import com.campus.campussharebackend.dto.ItemQueryDTO;
import com.campus.campussharebackend.dto.ItemStatusDTO;
import com.campus.campussharebackend.entity.Item;
import com.campus.campussharebackend.vo.*;

import java.util.List;

public interface ItemService extends IService<Item> {
    // 发布物品（含图片）
    Long publishItem(ItemPublishDTO publishDTO, Long userId);

    // 管理员审核物品（更新状态为"可借用"）
    void auditItem(Long itemId, Long operatorId);

    // 发布者下架物品（更新状态为"已下架"）
    void offlineItem(Long itemId, Long operatorId);

    // 分页查询物品列表
    IPage<ItemListVO> queryItemList(ItemQueryDTO queryDTO);

    // 查询物品详情
    ItemDetailVO getItemDetail(Long itemId);

    /**
     * 获取物品状态统计数据
     */
    List<ItemStatusCountVO> getItemStatusStats();

    /**
     * 获取分类物品数量统计
     */
    List<CategoryItemCountVO> getCategoryItemStats();

    /**
     * 获取统计概览（整合各类统计数据）
     */
    StatsOverviewVO getStatsOverview();
}