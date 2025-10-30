package com.campus.campussharebackend.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data // Lombok会自动生成getReturnTime()
public class ReturnConfirmDTO {
    @NotNull(message = "实际归还时间不能为空")
    private LocalDate returnTime; // 字段名必须为returnTime，才能生成getReturnTime()
}