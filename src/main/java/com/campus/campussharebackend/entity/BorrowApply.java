package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("borrow_apply")
public class BorrowApply {
    @TableId(type = IdType.AUTO)
    private Long id;                 // 申请ID（自增主键）

    @TableField("item_id")
    private Long itemId;             // 物品ID（关联item表）

    @TableField("user_id")
    private Long userId;             // 申请人ID（关联user表）

    @TableField("apply_time")
    private LocalDateTime applyTime; // 申请提交时间（默认当前时间）

    @TableField("expected_borrow_time")
    private LocalDate expectedBorrowTime; // 期望借用时间

    @TableField("expected_return_time")
    private LocalDate expectedReturnTime; // 期望归还时间

    private Integer status;          // 申请状态（0-待审核，1-已同意，2-已拒绝）

    @TableField("reject_reason")
    private String rejectReason;     // 拒绝原因（状态为2时必填）

    @TableField("create_time")
    private LocalDateTime createTime; // 记录创建时间

    @TableField("update_time")
    private LocalDateTime updateTime; // 记录更新时间（审核后更新）
}