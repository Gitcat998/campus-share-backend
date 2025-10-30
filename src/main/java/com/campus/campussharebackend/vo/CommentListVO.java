package com.campus.campussharebackend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentListVO {
    private Long id;             // 评价ID
    private Long userId;         // 评价人ID
    private String userName;     // 评价人姓名
    private String userAvatar;   // 评价人头像
    private String content;      // 评价内容
    private Integer score;       // 评分
    private LocalDateTime createTime; // 评价时间
}