package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("item")
public class Item {
    @TableId(type = IdType.AUTO)
    private Long id;                 // 物品ID
    private String name;             // 物品名称
    @TableField("item_type_id")
    private Long itemTypeId;         // 分类ID
    private String description;      // 物品描述
    private Integer status;          // 状态（0-待审核，1-可借用，2-已借出，3-已下架）
    @TableField("user_id")
    private Long userId;             // 发布者ID
    @TableField("borrow_duration")
    private Integer borrowDuration;  // 可借用时长（天，默认7）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField("is_delete")
    private Integer isDelete;        // 是否删除（0-未删，1-已删）
}