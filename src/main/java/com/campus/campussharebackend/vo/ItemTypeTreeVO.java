package com.campus.campussharebackend.vo;

import lombok.Data;
import java.util.List;

@Data
public class ItemTypeTreeVO {
    private Long id; // 分类ID
    private String typeName; // 分类名称
    private Integer sortOrder; // 排序序号
    private List<ItemTypeTreeVO> children; // 子分类（二级分类）
}