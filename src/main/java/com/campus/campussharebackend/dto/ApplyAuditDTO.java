package com.campus.campussharebackend.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class ApplyAuditDTO {
    @NotNull(message = "审核状态不能为空")
    private Integer status; // 字段名为status，Lombok会生成getStatus()

    private String rejectReason; // 拒绝原因（状态为2时必填）
}