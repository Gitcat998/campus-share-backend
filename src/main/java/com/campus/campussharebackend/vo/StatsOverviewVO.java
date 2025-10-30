package com.campus.campussharebackend.vo;

import lombok.Data;
import java.util.List;

@Data
public class StatsOverviewVO {
    // 评价统计（按物品）
    private List<CommentStatsVO> commentStats;
    // 分类物品数量统计
    private List<CategoryItemCountVO> categoryItemStats;
    // 物品状态统计
    private List<ItemStatusCountVO> itemStatusStats;
}