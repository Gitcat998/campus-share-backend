package com.campus.campussharebackend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.campus.campussharebackend.entity.BorrowRecord;
import com.campus.campussharebackend.vo.BorrowRecordVO;
import org.apache.ibatis.annotations.Param;

public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {
    // 分页查询借用记录
    IPage<BorrowRecordVO> selectRecordList(IPage<BorrowRecord> page, @Param(Constants.WRAPPER) Wrapper<BorrowRecord> queryWrapper);

    // 根据申请ID查询记录（申请通过后生成，一对一）
    BorrowRecord selectByApplyId(@Param("applyId") Long applyId); // 修正：@Param放在参数前
}