package com.campus.campussharebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.campussharebackend.dto.CommentSubmitDTO;
import com.campus.campussharebackend.service.CommentService;
import com.campus.campussharebackend.utils.JwtUtils;
import com.campus.campussharebackend.utils.Result;
import com.campus.campussharebackend.vo.ItemCommentStatsVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private JwtUtils jwtUtils;


    /**
     * 提交评价（需已归还物品）
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()") // 登录用户即可
    public Result submitComment(@Validated @RequestBody CommentSubmitDTO dto) {
        Long userId = getCurrentUserId();
        commentService.submitComment(dto, userId);
        return Result.success("评价提交成功");
    }


    /**
     * 查询物品的评价统计（平均评分+评价列表）
     */
    @GetMapping("/item/{itemId}")
    public Result<ItemCommentStatsVO> getItemComments(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        ItemCommentStatsVO statsVO = commentService.getItemCommentStats(itemId, pageNum, pageSize);
        return Result.success(statsVO);
    }


    /**
     * 删除评价（仅评价人或管理员）
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public Result deleteComment(@PathVariable Long commentId) {
        Long operatorId = getCurrentUserId();
        String roleName = jwtUtils.getRoleNameFromToken(getToken());
        Boolean isAdmin = "ADMIN".equals(roleName); // 是否为管理员

        commentService.deleteComment(commentId, operatorId, isAdmin);
        return Result.success("评价已删除");
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