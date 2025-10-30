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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/borrows")
public class BorrowController {

    private static final Logger log = LoggerFactory.getLogger(BorrowController.class);

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
            @RequestParam(required = false) Integer status) {

        Long userId = getCurrentUserId();
        String roleName = jwtUtils.getRoleNameFromToken(getToken());

        // 修复：判断角色是否为"管理员"
        Integer isAdmin = "管理员".equals(roleName) ? 1 : 0;

        log.info("=== 查询申请列表 Controller ===");
        log.info("当前用户ID: {}, 角色: {}, 是否为管理员: {}", userId, roleName, isAdmin);
        log.info("查询参数 - pageNum: {}, pageSize: {}, status: {}", pageNum, pageSize, status);

        // 调用Service时传递status参数
        IPage<BorrowApplyListVO> applyPage = borrowApplyService.queryApplyList(
                pageNum, pageSize, userId, isAdmin, status);

        log.info("Controller返回 - 总记录数: {}, 当前页记录数: {}",
                applyPage.getTotal(), applyPage.getRecords().size());
        log.info("=== 查询申请列表 Controller 结束 ===");

        return Result.success(applyPage);
    }

    /**
     * 审核借用申请（管理员）
     */
    @PutMapping("/applies/{applyId}/audit")
    @PreAuthorize("hasRole('管理员')")
    public Result auditApply(
            @PathVariable Long applyId,
            @Validated @RequestBody ApplyAuditDTO auditDTO) {

        log.info("审核申请 - applyId: {}, status: {}", applyId, auditDTO.getStatus());

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

        log.info("查询借阅记录 - pageNum: {}, pageSize: {}, userId: {}", pageNum, pageSize, userId);

        // 管理员可查询所有记录，学生只能查询自己的（通过userId筛选）
        String roleName = jwtUtils.getRoleNameFromToken(getToken());
        if (!"管理员".equals(roleName)) {
            userId = getCurrentUserId(); // 学生只能看自己的
            log.info("非管理员，设置userId为当前用户: {}", userId);
        }

        IPage<BorrowRecordVO> recordPage = borrowRecordService.queryRecordList(pageNum, pageSize, userId);
        log.info("借阅记录查询结果 - 总记录数: {}", recordPage.getTotal());

        return Result.success(recordPage);
    }

    /**
     * 确认归还物品（管理员）
     */
    @PutMapping("/records/{recordId}/return")
    @PreAuthorize("hasRole('管理员')")
    public Result confirmReturn(
            @PathVariable Long recordId,
            @Validated @RequestBody ReturnConfirmDTO dto) {

        log.info("确认归还 - recordId: {}", recordId);

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