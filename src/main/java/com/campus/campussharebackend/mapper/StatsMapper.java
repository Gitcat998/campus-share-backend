package com.campus.campussharebackend.mapper;

import com.campus.campussharebackend.vo.CategoryItemCountVO;
import com.campus.campussharebackend.vo.CommentStatsVO;
import com.campus.campussharebackend.vo.ItemStatusCountVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StatsMapper {

    /**
     * 统计单个物品的评价数量和平均评分
     * （按物品分组，过滤已删除的物品和评价）
     */
    @Select("select " +
            "i.id as itemId, " +
            "i.name as itemName, " +
            "count(c.id) as commentCount, " +
            "round(avg(c.score), 1) as avgScore " +
            "from item i " +
            "left join comment c on i.id = c.item_id and c.is_delete = 0 " +
            "where i.is_delete = 0 " +
            "group by i.id, i.name " +
            "order by commentCount desc")
    List<CommentStatsVO> selectCommentStats();

    /**
     * 统计各分类的物品数量（修复表名和字段名）
     * （过滤已删除的物品）
     */
    @Select("select " +
            "t.id as categoryId, " +  // 分类表主键
            "t.type_name as categoryName, " +  // 分类名称（实际字段是 type_name）
            "count(i.id) as itemCount " +
            "from item_type t " +  // 修复表名：category → item_type（数据库中实际表名）
            "left join item i on t.id = i.item_type_id and i.is_delete = 0 " +  // 修复关联字段：category_id → item_type_id
            "group by t.id, t.type_name " +  // 分组字段同步修正
            "order by itemCount desc")
    List<CategoryItemCountVO> selectCategoryItemCount();

    /**
     * 统计不同状态的物品数量
     * （补充状态0-待审核，与数据库状态定义一致）
     */
    @Select("select " +
            "status, " +
            "case status " +
            "  when 0 then '待审核' " +  // 补充数据库中存在的状态0
            "  when 1 then '可借用' " +
            "  when 2 then '已借出' " +
            "  when 3 then '已下架' " +  // 补充数据库中存在的状态3
            "  else '未知' end as statusName, " +
            "count(id) as count " +
            "from item " +
            "where is_delete = 0 " +
            "group by status " +
            "order by status")
    List<ItemStatusCountVO> selectItemStatusCount();
}