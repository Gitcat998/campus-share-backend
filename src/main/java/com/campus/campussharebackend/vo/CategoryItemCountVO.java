package com.campus.campussharebackend.vo;

import lombok.Data;

/**
 * 分类物品数量统计视图对象
 */
@Data
public class CategoryItemCountVO {
    private Long categoryId;     // 分类ID（关联category表的id）
    private String categoryName; // 分类名称（关联category表的name）
    private Integer itemCount;   // 该分类下的物品数量（过滤已删除物品）
}