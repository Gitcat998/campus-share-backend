package com.campus.campussharebackend.vo;

import lombok.Data;

@Data
public class CommentStatsVO {
    private Long itemId;         // 物品ID
    private String itemName;     // 物品名称
    private Integer commentCount; // 评价总数
    private Double avgScore;     // 平均评分（保留1位小数）
}