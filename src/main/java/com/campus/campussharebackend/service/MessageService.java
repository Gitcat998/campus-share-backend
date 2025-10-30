package com.campus.campussharebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.campussharebackend.dto.MessageOperateDTO;
import com.campus.campussharebackend.entity.Message;
import com.campus.campussharebackend.vo.MessageVO;

public interface MessageService extends IService<Message> {
    // 生成系统消息（供其他模块调用）
    void createMessage(Long userId, String title, String content, Integer messageType);

    // 查询用户的消息列表（支持按已读状态筛选）
    IPage<MessageVO> getUserMessages(Long userId, Integer isRead, Integer pageNum, Integer pageSize);

    // 标记消息为已读（单个）
    void markAsRead(Long messageId, Long userId);

    // 批量标记已读
    void batchMarkAsRead(MessageOperateDTO dto, Long userId);

    // 批量删除消息
    void batchDeleteMessages(MessageOperateDTO dto, Long userId);
}