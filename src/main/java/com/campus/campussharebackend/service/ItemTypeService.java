package com.campus.campussharebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.campussharebackend.dto.ItemTypeDTO;
import com.campus.campussharebackend.entity.ItemType;
import com.campus.campussharebackend.vo.ItemTypeTreeVO;

import java.util.List;

public interface ItemTypeService extends IService<ItemType> {
    // 新增分类
    void addType(ItemTypeDTO dto);

    // 修改分类
    void updateType(ItemTypeDTO dto);

    // 删除分类
    void deleteType(Long typeId);

    // 获取分类树形结构（一级+二级）
    List<ItemTypeTreeVO> getTypeTree();
}