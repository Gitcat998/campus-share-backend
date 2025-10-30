package com.campus.campussharebackend.service;

import com.campus.campussharebackend.vo.CategoryItemCountVO;
import com.campus.campussharebackend.vo.CommentStatsVO;
import com.campus.campussharebackend.vo.ItemStatusCountVO;
import com.campus.campussharebackend.vo.StatsOverviewVO;

import java.util.List;

public interface StatsService {
    // 获取单个物品的评价统计
    List<CommentStatsVO> getCommentStats();

    // 获取分类物品数量统计
    List<CategoryItemCountVO> getCategoryItemCount();

    // 获取物品状态统计
    List<ItemStatusCountVO> getItemStatusCount();

    // 获取所有统计数据总览
    StatsOverviewVO getStatsOverview();
}