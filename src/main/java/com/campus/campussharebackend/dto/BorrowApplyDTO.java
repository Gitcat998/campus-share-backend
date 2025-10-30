package com.campus.campussharebackend.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class BorrowApplyDTO {
    @NotNull(message = "物品ID不能为空")
    private Long itemId; // 目标物品ID

    @NotNull(message = "期望借用时间不能为空")
    private LocalDate expectedBorrowTime; // 期望借用开始时间

    @NotNull(message = "期望归还时间不能为空")
    private LocalDate expectedReturnTime; // 期望归还时间
}