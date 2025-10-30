package com.campus.campussharebackend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;             // 消息ID
    private String title;        // 标题
    private String content;      // 内容
    private Integer isRead;      // 是否已读（0-未读，1-已读）
    private String isReadText;   // 已读状态文本（未读/已读）
    private Integer messageType; // 消息类型
    private String messageTypeText; // 类型文本（借用通知/归还通知/系统通知）
    private LocalDateTime createTime; // 消息生成时间
}