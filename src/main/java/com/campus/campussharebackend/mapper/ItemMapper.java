package com.campus.campussharebackend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.campus.campussharebackend.entity.Item;
import com.campus.campussharebackend.vo.CategoryItemCountVO;
import com.campus.campussharebackend.vo.ItemListVO;
import com.campus.campussharebackend.vo.ItemStatusCountVO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemMapper extends BaseMapper<Item> {

    // 分页查询物品列表（关联 item_image 表获取主图）
    @Select("SELECT " +
            "i.id, " +
            "i.name, " +
            "i.status, " +
            // 从 item_image 表中获取主图 URL（i2 为 item_image 表别名）
            "i2.image_url AS mainImageUrl, " +
            "t.type_name AS typeName, " +
            "u.real_name AS publisherName " +
            "FROM item i " +
            "LEFT JOIN item_type t ON i.item_type_id = t.id " +
            "LEFT JOIN user u ON i.user_id = u.id " +  // 注意：数据库中是 user_id，不是 publisher_id
            // 关联 item_image 表，筛选主图（is_main = 1）
            "LEFT JOIN item_image i2 ON i.id = i2.item_id AND i2.is_main = 1 " +
            "${ew.customSqlSegment}")
    IPage<ItemListVO> selectItemList(IPage<Item> page, @Param(Constants.WRAPPER) Wrapper<Item> queryWrapper);

    // 根据物品ID查询详情（同样关联 item_image 表）
    @Select("SELECT " +
            "i.id, " +
            "i.name, " +
            "i.status, " +
            "i.description, " +
            "i.borrow_duration AS maxBorrowDays,  " +  // 数据库中是 borrow_duration，不是 max_borrow_days
            "t.type_name AS typeName, " +
            "u.real_name AS publisherName, " +
            "u.id AS publisherId, " +
            "u.avatar AS publisherAvatar, " +
            "i2.image_url AS mainImageUrl " +  // 主图URL
            "FROM item i " +
            "LEFT JOIN item_type t ON i.item_type_id = t.id " +
            "LEFT JOIN user u ON i.user_id = u.id " +  // 数据库中是 user_id
            "LEFT JOIN item_image i2 ON i.id = i2.item_id AND i2.is_main = 1 " +  // 主图
            "WHERE i.id = #{itemId} AND i.is_delete = 0")
    ItemListVO selectItemDetailById(Long itemId);


    /**
     * 统计各状态的物品数量（过滤已删除物品）
     */
    @Select("SELECT " +
            "status, " +
            "CASE " +
            "WHEN status = 0 THEN '待审核' " +
            "WHEN status = 1 THEN '可借用' " +
            "WHEN status = 2 THEN '已借出' " +
            "WHEN status = 3 THEN '已下架' " +
            "ELSE '未知状态' END AS statusName, " +
            "COUNT(*) AS count " +
            "FROM item " +
            "WHERE is_delete = 0 " +
            "GROUP BY status")
    List<ItemStatusCountVO> selectItemStatusStats();

    /**
     * 按分类统计物品数量（过滤已删除物品）
     */
    @Select("SELECT " +
            "t.id AS categoryId, " +
            "t.type_name AS categoryName, " +
            "COUNT(i.id) AS count " +
            "FROM item_type t " +
            "LEFT JOIN item i ON t.id = i.item_type_id AND i.is_delete = 0 " +
            "GROUP BY t.id, t.type_name " +
            "ORDER BY count DESC")
    List<CategoryItemCountVO> selectCategoryItemStats();
}