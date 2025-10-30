package com.campus.campussharebackend.dto;

import lombok.Data;

@Data
public class ItemQueryDTO {
    private Long itemTypeId;  // 分类ID（可选）
    private Integer status;   // 状态（可选）
    private String name;      // 名称模糊查询（可选）
    private Integer pageNum = 1;  // 页码（默认1）
    private Integer pageSize = 10; // 每页条数（默认10）
}