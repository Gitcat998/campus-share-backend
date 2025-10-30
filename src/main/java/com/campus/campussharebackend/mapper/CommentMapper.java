package com.campus.campussharebackend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.campus.campussharebackend.entity.Comment;
import com.campus.campussharebackend.vo.CommentListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CommentMapper extends BaseMapper<Comment> {

    // 分页查询物品的评价列表（关联评价人信息）
    @Select("SELECT " +
            "c.id, " +
            "c.item_id AS itemId, " +
            "c.user_id AS userId, " +
            "u.real_name AS userName, " +
            "u.avatar AS userAvatar, " +
            "c.content, " +
            "c.score, " +
            "c.create_time AS createTime " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "${ew.customSqlSegment}")
    IPage<CommentListVO> selectItemComments(IPage<Comment> page, @Param(Constants.WRAPPER) Wrapper<Comment> queryWrapper);

    // 查询物品的平均评分
    @Select("select avg(score) from comment where item_id = #{itemId} and is_delete = 0")
    Double selectAvgScoreByItemId(Long itemId);

    // 查询物品的评价总数
    @Select("select count(*) from comment where item_id = #{itemId} and is_delete = 0")
    Integer selectCommentCountByItemId(Long itemId);


}