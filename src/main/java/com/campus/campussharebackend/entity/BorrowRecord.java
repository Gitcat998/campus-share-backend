package com.campus.campussharebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("borrow_record")
public class BorrowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;                 // 记录ID（自增主键）

    @TableField("apply_id")
    private Long applyId;            // 关联的申请ID（一对一，关联borrow_apply表）

    @TableField("start_time")
    private LocalDate startTime;     // 实际借用开始时间（通常=申请的期望借用时间）

    @TableField("end_time")
    private LocalDate endTime;       // 计划归还时间（通常=申请的期望归还时间）

    @TableField("return_time")
    private LocalDate returnTime;    // 实际归还时间（确认归还后填写）

    private Integer status;          // 记录状态（0-借用中，1-已归还，2-超期未还）

    @TableField("create_time")
    private LocalDateTime createTime; // 记录创建时间（申请通过时生成）

    @TableField("update_time")
    private LocalDateTime updateTime; // 记录更新时间（确认归还后更新）
}