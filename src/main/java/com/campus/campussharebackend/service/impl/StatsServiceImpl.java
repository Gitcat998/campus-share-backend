package com.campus.campussharebackend.service.impl;

import com.campus.campussharebackend.mapper.StatsMapper;
import com.campus.campussharebackend.service.StatsService;
import com.campus.campussharebackend.vo.CategoryItemCountVO;
import com.campus.campussharebackend.vo.CommentStatsVO;
import com.campus.campussharebackend.vo.ItemStatusCountVO;
import com.campus.campussharebackend.vo.StatsOverviewVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StatsServiceImpl implements StatsService {

    @Resource
    private StatsMapper statsMapper;


    @Override
    public List<CommentStatsVO> getCommentStats() {
        // 调用Mapper获取评价统计数据
        return statsMapper.selectCommentStats();
    }


    @Override
    public List<CategoryItemCountVO> getCategoryItemCount() {
        // 调用Mapper获取分类物品数量统计
        return statsMapper.selectCategoryItemCount();
    }


    @Override
    public List<ItemStatusCountVO> getItemStatusCount() {
        // 调用Mapper获取物品状态统计
        return statsMapper.selectItemStatusCount();
    }


    @Override
    public StatsOverviewVO getStatsOverview() {
        // 整合所有统计数据
        StatsOverviewVO overview = new StatsOverviewVO();
        overview.setCommentStats(getCommentStats());
        overview.setCategoryItemStats(getCategoryItemCount());
        overview.setItemStatusStats(getItemStatusCount());
        return overview;
    }
}