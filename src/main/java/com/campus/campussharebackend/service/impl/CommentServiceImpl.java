package com.campus.campussharebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.campussharebackend.dto.CommentSubmitDTO;
import com.campus.campussharebackend.entity.Comment;
import com.campus.campussharebackend.entity.Item;
import com.campus.campussharebackend.entity.BorrowRecord;
import com.campus.campussharebackend.exception.BusinessException;
import com.campus.campussharebackend.mapper.CommentMapper;
import com.campus.campussharebackend.mapper.ItemMapper;
import com.campus.campussharebackend.mapper.BorrowRecordMapper;
import com.campus.campussharebackend.mapper.UserMapper;
import com.campus.campussharebackend.service.CommentService;
import com.campus.campussharebackend.vo.CommentListVO;
import com.campus.campussharebackend.vo.ItemCommentStatsVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private BorrowRecordMapper borrowRecordMapper;

    @Resource
    private UserMapper userMapper;


    @Override
    @Transactional
    public void submitComment(CommentSubmitDTO dto, Long userId) {
        // 1. 校验物品存在
        Item item = itemMapper.selectById(dto.getItemId());
        if (item == null || item.getIsDelete() == 1) {
            throw new BusinessException("物品不存在");
        }

        // 2. 校验用户是否已归还该物品（只有归还后才能评价）
        QueryWrapper<BorrowRecord> recordWrapper = new QueryWrapper<>();
        recordWrapper.eq("user_id", userId)
                .eq("status", 1) // 状态=1（已归还）
                .inSql("apply_id", "select id from borrow_apply where item_id = " + dto.getItemId());
        long returnedCount = borrowRecordMapper.selectCount(recordWrapper);
        if (returnedCount == 0) {
            throw new BusinessException("只有归还物品后才能评价");
        }

        // 3. 校验是否重复评价
        QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
        commentWrapper.eq("item_id", dto.getItemId())
                .eq("user_id", userId)
                .eq("is_delete", 0);
        if (commentMapper.selectCount(commentWrapper) > 0) {
            throw new BusinessException("已评价过该物品，不可重复评价");
        }

        // 4. 保存评价
        Comment comment = new Comment();
        comment.setItemId(dto.getItemId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setScore(dto.getScore());
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setIsDelete(0);
        commentMapper.insert(comment);
    }


    @Override
    public ItemCommentStatsVO getItemCommentStats(Long itemId, Integer pageNum, Integer pageSize) {
        // 1. 校验物品存在
        Item item = itemMapper.selectById(itemId);
        if (item == null || item.getIsDelete() == 1) {
            throw new BusinessException("物品不存在");
        }

        // 2. 查询平均评分（保留1位小数）
        Double avgScore = commentMapper.selectAvgScoreByItemId(itemId);
        if (avgScore == null) avgScore = 0.0;
        avgScore = new BigDecimal(avgScore).setScale(1, RoundingMode.HALF_UP).doubleValue();

        // 3. 查询评价总数
        Integer commentCount = commentMapper.selectCommentCountByItemId(itemId);

        // 4. 分页查询评价列表（按时间倒序）
        IPage<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        // 为字段添加表别名c.（对应comment表），解决多表关联时的字段歧义
        queryWrapper.eq("c.item_id", itemId)  // 明确指定comment表的item_id
                .eq("c.is_delete", 0)         // 明确指定comment表的is_delete
                .orderByDesc("c.create_time"); // 明确指定comment表的create_time

        IPage<CommentListVO> commentPage = commentMapper.selectItemComments(page, queryWrapper);

        // 5. 封装结果
        ItemCommentStatsVO statsVO = new ItemCommentStatsVO();
        statsVO.setAvgScore(avgScore);
        statsVO.setCommentCount(commentCount);
        statsVO.setComments(commentPage.getRecords());

        return statsVO;
    }


    @Override
    @Transactional
    public void deleteComment(Long commentId, Long operatorId, Boolean isAdmin) {
        // 1. 校验评价存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDelete() == 1) {
            throw new BusinessException("评价不存在");
        }

        // 2. 权限校验（仅评价人或管理员可删除）
        if (!isAdmin && !comment.getUserId().equals(operatorId)) {
            throw new BusinessException("无权限删除该评价");
        }

        // 3. 逻辑删除（更新is_delete=1）
        comment.setIsDelete(1);
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);
    }
}