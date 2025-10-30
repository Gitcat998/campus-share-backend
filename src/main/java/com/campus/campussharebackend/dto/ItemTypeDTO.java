package com.campus.campussharebackend.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ItemTypeDTO {
    private Long id; // 新增时无需传，修改时必传

    @NotBlank(message = "分类名称不能为空")
    private String typeName; // 分类名称（如"课本教材"）

    @NotNull(message = "父分类ID不能为空")
    private Long parentId; // 父分类ID（0表示一级分类）

    private Integer sortOrder = 0; // 排序序号（默认0）
}