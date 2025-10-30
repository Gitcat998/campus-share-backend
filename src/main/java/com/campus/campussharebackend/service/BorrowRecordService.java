package com.campus.campussharebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.campussharebackend.dto.ReturnConfirmDTO;
import com.campus.campussharebackend.entity.BorrowRecord;
import com.campus.campussharebackend.vo.BorrowRecordVO;

public interface BorrowRecordService extends IService<BorrowRecord> {
    // 确认归还物品
    void confirmReturn(Long recordId, ReturnConfirmDTO dto, Long adminId);

    // 分页查询借用记录（支持按用户筛选）
    IPage<BorrowRecordVO> queryRecordList(Integer pageNum, Integer pageSize, Long userId);
}