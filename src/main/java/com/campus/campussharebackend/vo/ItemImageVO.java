package com.campus.campussharebackend.vo;

import lombok.Data;

@Data
public class ItemImageVO {
    private Long id;             // 图片ID
    private String imageUrl;     // 图片访问URL（前端可直接用于img标签）
    private Integer isMain;      // 是否主图（0-否，1-是）
}