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
     * 统计各分类的物品数量
     * （过滤已删除的物品）
     */
    @Select("select " +
            "c.id as categoryId, " +
            "c.name as categoryName, " +
            "count(i.id) as itemCount " +
            "from category c " +
            "left join item i on c.id = i.category_id and i.is_delete = 0 " +
            "group by c.id, c.name " +
            "order by itemCount desc")
    List<CategoryItemCountVO> selectCategoryItemCount();

    /**
     * 统计不同状态的物品数量
     * （1-可借用，2-已借出，过滤已删除的物品）
     */
    @Select("select " +
            "status, " +
            "case status when 1 then '可借用' when 2 then '已借出' else '未知' end as statusName, " +
            "count(id) as count " +
            "from item " +
            "where is_delete = 0 " +
            "group by status " +
            "order by status")
    List<ItemStatusCountVO> selectItemStatusCount();
}