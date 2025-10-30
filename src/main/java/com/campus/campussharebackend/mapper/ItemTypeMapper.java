package com.campus.campussharebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.campussharebackend.entity.ItemType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ItemTypeMapper extends BaseMapper<ItemType> {
    // 根据分类ID查询分类名称
    @Select("select type_name from item_type where id = #{typeId}")
    String selectTypeNameById(Long typeId);

    // 根据父分类ID查询子分类
    @Select("select * from item_type where parent_id = #{parentId} order by sort_order asc")
    List<ItemType> selectByParentId(Long parentId);

    // 校验同一父分类下是否存在重复名称
    @Select("select count(*) from item_type where type_name = #{typeName} and parent_id = #{parentId} and id != #{id}")
    int countByTypeNameAndParentId(String typeName, Long parentId, Long id);

    // 查询一级分类（parent_id=0）
    @Select("select * from item_type where parent_id = 0 order by sort_order asc")
    List<ItemType> selectFirstLevelTypes();

    // 检查分类是否关联物品（item表中是否有引用）
    @Select("select count(*) from item where item_type_id = #{typeId} and is_delete = 0")
    int countRelatedItems(Long typeId);

    // 检查是否有子分类（用于删除一级分类时校验）
    @Select("select count(*) from item_type where parent_id = #{typeId}")
    int countChildren(Long typeId);
}