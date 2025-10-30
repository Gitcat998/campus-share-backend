package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;             // 消息ID

    @TableField("user_id")
    private Long userId;         // 接收用户ID（关联user表）

    private String title;        // 消息标题

    private String content;      // 消息内容

    @TableField("is_read")
    private Integer isRead;      // 是否已读（0-未读，1-已读）

    @TableField("message_type")
    private Integer messageType; // 消息类型（0-借用通知，1-归还通知，2-系统通知）

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}