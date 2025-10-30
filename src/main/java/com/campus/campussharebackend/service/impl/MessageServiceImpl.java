package com.campus.campussharebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.campussharebackend.dto.MessageOperateDTO;
import com.campus.campussharebackend.entity.Message;
import com.campus.campussharebackend.entity.User;
import com.campus.campussharebackend.exception.BusinessException;
import com.campus.campussharebackend.mapper.MessageMapper;
import com.campus.campussharebackend.mapper.UserMapper;
import com.campus.campussharebackend.service.MessageService;
import com.campus.campussharebackend.vo.MessageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private UserMapper userMapper;


    @Override
    @Transactional
    public void createMessage(Long userId, String title, String content, Integer messageType) {
        // 校验用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("接收消息的用户不存在");
        }

        // 校验消息类型合法（0-借用通知，1-归还通知，2-系统通知）
        if (messageType < 0 || messageType > 2) {
            throw new BusinessException("消息类型不合法");
        }

        // 保存消息（默认未读）
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setIsRead(0); // 初始状态：未读
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }


    @Override
    public IPage<MessageVO> getUserMessages(Long userId, Integer isRead, Integer pageNum, Integer pageSize) {
        // 分页条件
        IPage<Message> page = new Page<>(pageNum, pageSize);

        // 查询条件：用户ID + 已读状态（可选）
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (isRead != null) {
            queryWrapper.eq("is_read", isRead);
        }
        queryWrapper.orderByDesc("create_time"); // 按时间倒序（最新消息在前）

        // 分页查询
        IPage<MessageVO> messagePage = messageMapper.selectUserMessages(page, queryWrapper);

        // 转换状态文本（已读/未读 + 消息类型）
        messagePage.getRecords().forEach(vo -> {
            vo.setIsReadText(vo.getIsRead() == 0 ? "未读" : "已读");
            vo.setMessageTypeText(convertMessageType(vo.getMessageType()));
        });

        return messagePage;
    }


    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        // 校验消息存在且属于当前用户
        Message message = messageMapper.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new BusinessException("消息不存在或无权操作");
        }

        // 已读无需重复操作
        if (message.getIsRead() == 1) {
            return;
        }

        // 更新为已读
        message.setIsRead(1);
        message.setUpdateTime(LocalDateTime.now());
        messageMapper.updateById(message);
    }


    @Override
    @Transactional
    public void batchMarkAsRead(MessageOperateDTO dto, Long userId) {
        // 批量更新已读（通过Mapper的批量SQL）
        messageMapper.batchMarkRead(dto.getMessageIds(), userId);
    }


    @Override
    @Transactional
    public void batchDeleteMessages(MessageOperateDTO dto, Long userId) {
        // 批量逻辑删除（仅删除当前用户的消息）
        messageMapper.batchDelete(dto.getMessageIds(), userId);
    }


    // 消息类型转换为文本
    private String convertMessageType(Integer type) {
        switch (type) {
            case 0: return "借用通知";
            case 1: return "归还通知";
            case 2: return "系统通知";
            default: return "未知类型";
        }
    }
}