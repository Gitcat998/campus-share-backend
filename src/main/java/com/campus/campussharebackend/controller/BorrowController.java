package com.campus.campussharebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.campussharebackend.dto.ApplyAuditDTO;
import com.campus.campussharebackend.dto.BorrowApplyDTO;
import com.campus.campussharebackend.dto.ReturnConfirmDTO;
import com.campus.campussharebackend.service.BorrowApplyService;
import com.campus.campussharebackend.service.BorrowRecordService;
import com.campus.campussharebackend.utils.JwtUtils;
import com.campus.campussharebackend.utils.Result;
import com.campus.campussharebackend.vo.BorrowApplyListVO;
import com.campus.campussharebackend.vo.BorrowRecordVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/borrows")
public class BorrowController {

    @Resource
    private BorrowApplyService borrowApplyService;

    @Resource
    private BorrowRecordService borrowRecordService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private JwtUtils jwtUtils;


    /**
     * 提交借用申请（学生权限）
     */
    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('PERMISSION_item:borrow')")
    public Result<Long> submitApply(@Validated @RequestBody BorrowApplyDTO dto) {
        Long userId = getCurrentUserId();
        Long applyId = borrowApplyService.submitApply(dto, userId);
        // 调用新增的重载方法：返回数据（applyId）+ 自定义消息
        return Result.success(applyId, "申请提交成功，等待管理员审核");
    }


    /**
     * 分页查询借用申请列表
     * - 管理员：查看所有申请
     * - 学生：查看自己的申请
     */
    @GetMapping("/applies")
    public Result<IPage<BorrowApplyListVO>> queryApplyList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) { // 新增：接收status筛选参数

        Long userId = getCurrentUserId();
        String roleName = jwtUtils.getRoleNameFromToken(getToken());
        Integer isAdmin = "ADMIN".equals(roleName) ? 1 : 0;

        // 调用Service时传递status参数
        IPage<BorrowApplyListVO> applyPage = borrowApplyService.queryApplyList(
                pageNum, pageSize, userId, isAdmin, status);
        return Result.success(applyPage);
    }


    /**
     * 审核借用申请（管理员）
     */
    @PutMapping("/applies/{applyId}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result auditApply(
            @PathVariable Long applyId,
            @Validated @RequestBody ApplyAuditDTO auditDTO) {

        Long adminId = getCurrentUserId();
        borrowApplyService.auditApply(applyId, auditDTO, adminId);
        // 正确调用getStatus()（前提：ApplyAuditDTO中字段为status）
        String msg = auditDTO.getStatus() == 1 ? "申请已同意" : "申请已拒绝";
        return Result.success(msg);
    }


    /**
     * 分页查询借用记录
     */
    @GetMapping("/records")
    public Result<IPage<BorrowRecordVO>> queryRecordList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId) {

        // 管理员可查询所有记录，学生只能查询自己的（通过userId筛选）
        String roleName = jwtUtils.getRoleNameFromToken(getToken());
        if (!"ADMIN".equals(roleName)) {
            userId = getCurrentUserId(); // 学生只能看自己的
        }

        IPage<BorrowRecordVO> recordPage = borrowRecordService.queryRecordList(pageNum, pageSize, userId);
        return Result.success(recordPage);
    }


    /**
     * 确认归还物品（管理员）
     */
    @PutMapping("/records/{recordId}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public Result confirmReturn(
            @PathVariable Long recordId,
            @Validated @RequestBody ReturnConfirmDTO dto) {

        Long adminId = getCurrentUserId();
        borrowRecordService.confirmReturn(recordId, dto, adminId);
        return Result.success("归还确认成功");
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