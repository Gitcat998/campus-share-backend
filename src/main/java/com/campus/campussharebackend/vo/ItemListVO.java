package com.campus.campussharebackend.vo;

import lombok.Data;

@Data
public class ItemListVO {
    private Long id;             // 物品ID
    private String name;         // 物品名称
    private String typeName;     // 分类名称（如"课本教材"）
    private String mainImageUrl; // 主图URL
    private Integer status;      // 状态
    private String statusText;   // 状态文本（如"待审核"）
    private String publisherName;// 发布者姓名
    private Integer borrowDuration; // 可借用时长
}