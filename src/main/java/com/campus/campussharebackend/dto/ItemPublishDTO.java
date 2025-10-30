package com.campus.campussharebackend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ItemPublishDTO {
    @NotBlank(message = "物品名称不能为空")
    private String name;                 // 物品名称

    @NotNull(message = "分类ID不能为空")
    private Long itemTypeId;             // 分类ID

    private String description;          // 物品描述（可选）

    private Integer borrowDuration = 7;  // 可借用时长（默认7天）

    private List<MultipartFile> images;  // 图片文件（多图）

    private Integer mainImageIndex;      // 主图索引（如0表示第1张图为主图）
}