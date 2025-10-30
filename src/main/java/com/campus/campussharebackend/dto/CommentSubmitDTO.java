package com.campus.campussharebackend.dto;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CommentSubmitDTO {
    @NotNull(message = "物品ID不能为空")
    private Long itemId;         // 被评价物品ID

    private String content;      // 评价内容（可选）

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer score;       // 评分（1-5分）
}