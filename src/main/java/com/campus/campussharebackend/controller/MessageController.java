package com.campus.campussharebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.campussharebackend.dto.MessageOperateDTO;
import com.campus.campussharebackend.service.MessageService;
import com.campus.campussharebackend.utils.JwtUtils;
import com.campus.campussharebackend.utils.Result;
import com.campus.campussharebackend.vo.MessageVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private JwtUtils jwtUtils;


    /**
     * 查询用户的消息列表（支持筛选已读/未读）
     * isRead：0-未读，1-已读，null-全部
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<MessageVO>> getUserMessages(
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = getCurrentUserId();
        IPage<MessageVO> messagePage = messageService.getUserMessages(userId, isRead, pageNum, pageSize);
        return Result.success(messagePage);
    }


    /**
     * 标记单条消息为已读
     */
    @PutMapping("/{messageId}/read")
    @PreAuthorize("isAuthenticated()")
    public Result markAsRead(@PathVariable Long messageId) {
        Long userId = getCurrentUserId();
        messageService.markAsRead(messageId, userId);
        return Result.success("消息已标记为已读");
    }


    /**
     * 批量标记消息为已读
     */
    @PutMapping("/batch/read")
    @PreAuthorize("isAuthenticated()")
    public Result batchMarkAsRead(@Validated @RequestBody MessageOperateDTO dto) {
        Long userId = getCurrentUserId();
        messageService.batchMarkAsRead(dto, userId);
        return Result.success("已批量标记消息为已读");
    }


    /**
     * 批量删除消息
     */
    @DeleteMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public Result batchDelete(@Validated @RequestBody MessageOperateDTO dto) {
        Long userId = getCurrentUserId();
        messageService.batchDeleteMessages(dto, userId);
        return Result.success("已批量删除消息");
    }


    // 工具方法：获取当前用户ID
    private Long getCurrentUserId() {
        return jwtUtils.getUserIdFromToken(getToken());
    }

    // 工具方法：获取Token
    private String getToken() {
        return request.getHeader("Authorization").substring(7);
    }
}