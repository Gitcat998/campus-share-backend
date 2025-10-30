package com.campus.campussharebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.campussharebackend.dto.BorrowApplyDTO;
import com.campus.campussharebackend.dto.ApplyAuditDTO;
import com.campus.campussharebackend.entity.BorrowApply;
import com.campus.campussharebackend.vo.BorrowApplyListVO;

public interface BorrowApplyService extends IService<BorrowApply> {
    // 提交借用申请
    Long submitApply(BorrowApplyDTO dto, Long userId);

    // 审核借用申请（管理员）
    void auditApply(Long applyId, ApplyAuditDTO auditDTO, Long adminId);

    // 分页查询申请列表（管理员看所有，学生看自己的）
    IPage<BorrowApplyListVO> queryApplyList(
            Integer pageNum,
            Integer pageSize,
            Long userId,
            Integer isAdmin,
            Integer status);
}