package com.campus.campussharebackend.vo;

import lombok.Data;

/**
 * 物品状态统计视图对象
 */
@Data
public class ItemStatusCountVO {
    private Integer status;      // 物品状态码（1-可借用，2-已借出）
    private String statusName;   // 状态名称（如“可借用”“已借出”）
    private Integer count;       // 该状态的物品数量（过滤已删除物品）
}