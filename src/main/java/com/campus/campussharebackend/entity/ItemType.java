package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("item_type")
public class ItemType {
    @TableId(type = IdType.AUTO)
    private Long id;             // 分类ID
    private String typeName;     // 分类名称（如"课本教材"）
    private Long parentId;       // 父分类ID（0为一级分类）
    private Integer sortOrder;   // 排序序号
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}