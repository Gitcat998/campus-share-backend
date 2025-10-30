package com.campus.campussharebackend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ImageUploadDTO {
    @NotNull(message = "物品ID不能为空")
    private Long itemId;         // 关联的物品ID

    private List<MultipartFile> files; // 上传的图片文件列表（支持多图）

    private Integer mainImageIndex; // 主图索引（files列表中的位置，如0表示第一张图为主图）
}