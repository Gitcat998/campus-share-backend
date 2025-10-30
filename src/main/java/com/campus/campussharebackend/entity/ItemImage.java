package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("item_image")
public class ItemImage {
    @TableId(type = IdType.AUTO)
    private Long id;             // 图片ID

    @TableField("item_id")
    private Long itemId;         // 关联物品ID（外键关联item表）

    @TableField("image_url")
    private String imageUrl;     // 图片存储路径（相对路径或URL）

    @TableField("is_main")
    private Integer isMain;      // 是否主图（0-否，1-是），一个物品只能有1张主图

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}