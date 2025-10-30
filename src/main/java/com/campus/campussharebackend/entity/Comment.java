package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;             // 评价ID

    @TableField("item_id")
    private Long itemId;         // 物品ID（关联item表）

    @TableField("user_id")
    private Long userId;         // 评价人ID（关联user表）

    private String content;      // 评价内容

    private Integer score;       // 评分（1-5分）

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_delete")
    private Integer isDelete;    // 是否删除（0-未删，1-已删）
}