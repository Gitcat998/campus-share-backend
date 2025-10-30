package com.campus.campussharebackend.vo;

import lombok.Data;
import java.util.List;

@Data
public class ItemCommentStatsVO {
    private Double avgScore;             // 平均评分（保留1位小数）
    private Integer commentCount;        // 评价总数
    private List<CommentListVO> comments; // 评价列表
}