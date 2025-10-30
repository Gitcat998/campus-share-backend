package com.campus.campussharebackend.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class MessageOperateDTO {
    @NotEmpty(message = "消息ID列表不能为空")
    private List<Long> messageIds; // 消息ID列表
}