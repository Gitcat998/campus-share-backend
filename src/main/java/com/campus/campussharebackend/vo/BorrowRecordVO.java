package com.campus.campussharebackend.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BorrowRecordVO {
    private Long id; // 记录ID
    private Long applyId; // 关联申请ID
    private Long itemId; // 物品ID
    private String itemName; // 物品名称
    private Long userId; // 申请人ID
    private String username; // 申请人姓名
    private LocalDate startTime; // 实际借用开始时间
    private LocalDate endTime; // 计划归还时间
    private LocalDate returnTime; // 实际归还时间（可为空）
    private Integer status; // 记录状态（0-借用中，1-已归还，2-超期未还）
    private String statusText; // 状态文本
    private LocalDateTime createTime; // 记录创建时间
}