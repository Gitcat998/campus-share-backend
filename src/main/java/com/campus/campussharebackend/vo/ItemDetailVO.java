package com.campus.campussharebackend.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemDetailVO {
    // 物品基本信息
    private Long id;
    private String name;
    private String typeName;     // 分类名称
    private String description;
    private Integer status;
    private String statusText;
    private Integer borrowDuration;
    private LocalDateTime createTime;

    // 发布者信息
    private Long userId;
    private String publisherName; // 发布者姓名
    private String publisherAvatar; // 发布者头像

    // 图片列表
    private List<String> imageUrls; // 所有图片URL
    private String mainImageUrl;    // 主图URL

    // 评价信息（简化）
    private Integer commentCount;   // 评价数量
    private Double avgScore;        // 平均评分
}