package com.campus.campussharebackend.controller;

import com.campus.campussharebackend.service.StatsService;
import com.campus.campussharebackend.utils.Result;
import com.campus.campussharebackend.vo.CategoryItemCountVO;
import com.campus.campussharebackend.vo.CommentStatsVO;
import com.campus.campussharebackend.vo.ItemStatusCountVO;
import com.campus.campussharebackend.vo.StatsOverviewVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
@PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问统计接口
public class StatsController {

    @Resource
    private StatsService statsService;


    /**
     * 获取单个物品的评价统计（数量+平均评分）
     */
    @GetMapping("/comments")
    public Result<List<CommentStatsVO>> getCommentStats() {
        return Result.success(statsService.getCommentStats());
    }


    /**
     * 获取各分类的物品数量统计
     */
    @GetMapping("/items/category")
    public Result<List<CategoryItemCountVO>> getCategoryItemCount() {
        return Result.success(statsService.getCategoryItemCount());
    }


    /**
     * 获取不同状态的物品数量统计
     */
    @GetMapping("/items/status")
    public Result<List<ItemStatusCountVO>> getItemStatusCount() {
        return Result.success(statsService.getItemStatusCount());
    }


    /**
     * 获取所有统计数据总览
     */
    @GetMapping("/overview")
    public Result<StatsOverviewVO> getStatsOverview() {
        return Result.success(statsService.getStatsOverview());
    }
}