package com.campus.campussharebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.campussharebackend.dto.CommentSubmitDTO;
import com.campus.campussharebackend.entity.Comment;
import com.campus.campussharebackend.vo.CommentListVO;
import com.campus.campussharebackend.vo.ItemCommentStatsVO;

public interface CommentService extends IService<Comment> {
    // 提交评价（物品归还后）
    void submitComment(CommentSubmitDTO dto, Long userId);

    // 查询物品的评价统计（平均评分+评价列表）
    ItemCommentStatsVO getItemCommentStats(Long itemId, Integer pageNum, Integer pageSize);

    // 删除评价（仅评价人或管理员）
    void deleteComment(Long commentId, Long operatorId, Boolean isAdmin);
}