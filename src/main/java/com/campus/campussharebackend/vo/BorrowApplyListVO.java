package com.campus.campussharebackend.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BorrowApplyListVO {
    private Long id; // 申请ID
    private Long itemId; // 物品ID
    private String itemName; // 物品名称
    private String itemImage; // 物品主图
    private Long userId; // 申请人ID
    private String username; // 申请人姓名
    private LocalDate expectedBorrowTime; // 期望借用时间
    private LocalDate expectedReturnTime; // 期望归还时间
    private Integer status; // 申请状态（0-待审核，1-已同意，2-已拒绝）
    private String statusText; // 状态文本
    private LocalDateTime applyTime; // 申请提交时间
}