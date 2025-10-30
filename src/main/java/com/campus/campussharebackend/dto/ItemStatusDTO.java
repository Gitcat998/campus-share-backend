package com.campus.campussharebackend.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class ItemStatusDTO {
    @NotNull(message = "目标状态不能为空")
    private Integer status;  // 目标状态（1-可借用，3-已下架）
}