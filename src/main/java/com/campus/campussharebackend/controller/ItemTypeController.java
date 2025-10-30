package com.campus.campussharebackend.controller;

import com.campus.campussharebackend.dto.ItemTypeDTO;
import com.campus.campussharebackend.service.ItemTypeService;
import com.campus.campussharebackend.utils.Result;
import com.campus.campussharebackend.vo.ItemTypeTreeVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/v1/item-types")
public class ItemTypeController {

    @Resource
    private ItemTypeService itemTypeService;

    /**
     * 新增分类（仅管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可操作
    public Result addType(@Validated @RequestBody ItemTypeDTO dto) {
        itemTypeService.addType(dto);
        return Result.success("分类新增成功");
    }

    /**
     * 修改分类（仅管理员）
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result updateType(@Validated @RequestBody ItemTypeDTO dto) {
        itemTypeService.updateType(dto);
        return Result.success("分类修改成功");
    }

    /**
     * 删除分类（仅管理员）
     */
    @DeleteMapping("/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result deleteType(@PathVariable Long typeId) {
        itemTypeService.deleteType(typeId);
        return Result.success("分类删除成功");
    }

    /**
     * 获取分类树形结构（所有人可查询）
     */
    @GetMapping("/tree")
    public Result<List<ItemTypeTreeVO>> getTypeTree() {
        List<ItemTypeTreeVO> typeTree = itemTypeService.getTypeTree();
        return Result.success(typeTree);
    }
}