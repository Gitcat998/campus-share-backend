package com.campus.campussharebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.campussharebackend.dto.ItemTypeDTO;
import com.campus.campussharebackend.entity.ItemType;
import com.campus.campussharebackend.exception.BusinessException;
import com.campus.campussharebackend.mapper.ItemMapper;
import com.campus.campussharebackend.mapper.ItemTypeMapper;
import com.campus.campussharebackend.service.ItemTypeService;
import com.campus.campussharebackend.vo.ItemTypeTreeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemTypeServiceImpl extends ServiceImpl<ItemTypeMapper, ItemType> implements ItemTypeService {

    @Resource
    private ItemTypeMapper itemTypeMapper;

    @Resource
    private ItemMapper itemMapper; // 用于查询物品关联


    @Override
    @Transactional
    public void addType(ItemTypeDTO dto) {
        // 1. 校验同一父分类下名称是否重复
        int count = itemTypeMapper.countByTypeNameAndParentId(dto.getTypeName(), dto.getParentId(), -1L); // id=-1表示新增
        if (count > 0) {
            throw new BusinessException("该父分类下已存在同名分类");
        }

        // 2. 保存分类
        ItemType itemType = new ItemType();
        BeanUtils.copyProperties(dto, itemType);
        itemType.setCreateTime(LocalDateTime.now());
        itemType.setUpdateTime(LocalDateTime.now());
        itemTypeMapper.insert(itemType);
    }


    @Override
    @Transactional
    public void updateType(ItemTypeDTO dto) {
        // 1. 校验分类存在
        ItemType existingType = itemTypeMapper.selectById(dto.getId());
        if (existingType == null) {
            throw new BusinessException("分类不存在");
        }

        // 2. 校验同一父分类下名称是否重复（排除自身）
        int count = itemTypeMapper.countByTypeNameAndParentId(dto.getTypeName(), dto.getParentId(), dto.getId());
        if (count > 0) {
            throw new BusinessException("该父分类下已存在同名分类");
        }

        // 3. 更新分类
        BeanUtils.copyProperties(dto, existingType);
        existingType.setUpdateTime(LocalDateTime.now());
        itemTypeMapper.updateById(existingType);
    }


    @Override
    @Transactional
    public void deleteType(Long typeId) {
        // 1. 校验分类存在
        ItemType type = itemTypeMapper.selectById(typeId);
        if (type == null) {
            throw new BusinessException("分类不存在");
        }

        // 2. 校验是否有子分类（一级分类删除前需先删除二级分类）
        int childCount = itemTypeMapper.countChildren(typeId);
        if (childCount > 0) {
            throw new BusinessException("该分类下存在子分类，请先删除子分类");
        }

        // 3. 校验是否关联物品（有物品关联则不允许删除）
        int itemCount = itemTypeMapper.countRelatedItems(typeId);
        if (itemCount > 0) {
            throw new BusinessException("该分类已关联物品，无法删除");
        }

        // 4. 执行删除
        itemTypeMapper.deleteById(typeId);
    }


    @Override
    public List<ItemTypeTreeVO> getTypeTree() {
        // 1. 查询所有一级分类（parent_id=0）
        List<ItemType> firstLevelTypes = itemTypeMapper.selectFirstLevelTypes();
        List<ItemTypeTreeVO> treeVOList = new ArrayList<>();

        // 2. 为每个一级分类添加二级分类
        for (ItemType firstLevel : firstLevelTypes) {
            ItemTypeTreeVO firstLevelVO = new ItemTypeTreeVO();
            BeanUtils.copyProperties(firstLevel, firstLevelVO);

            // 查询当前一级分类的二级分类（parent_id=一级分类ID）
            List<ItemType> secondLevelTypes = itemTypeMapper.selectByParentId(firstLevel.getId());
            List<ItemTypeTreeVO> children = new ArrayList<>();
            for (ItemType secondLevel : secondLevelTypes) {
                ItemTypeTreeVO secondLevelVO = new ItemTypeTreeVO();
                BeanUtils.copyProperties(secondLevel, secondLevelVO);
                children.add(secondLevelVO);
            }

            firstLevelVO.setChildren(children);
            treeVOList.add(firstLevelVO);
        }

        return treeVOList;
    }
}