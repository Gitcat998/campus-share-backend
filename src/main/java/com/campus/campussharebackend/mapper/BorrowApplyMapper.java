package com.campus.campussharebackend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.campussharebackend.entity.BorrowApply;
import com.campus.campussharebackend.vo.BorrowApplyListVO;
import org.apache.ibatis.annotations.Param;

public interface BorrowApplyMapper extends BaseMapper<BorrowApply> {

    // 分页查询申请列表（主查询）
    IPage<BorrowApplyListVO> selectApplyList(
            Page<BorrowApplyListVO> page,
            @Param("userId") Long userId,
            @Param("isAdmin") Integer isAdmin,
            @Param("status") Integer status); // 新增：status参数

    // 手动声明 COUNT 方法（关键：与 XML 中的 selectApplyList_COUNT 对应）
    Long selectApplyList_COUNT(
            @Param("userId") Long userId,
            @Param("isAdmin") Integer isAdmin,
            @Param("status") Integer status);

    /**
     * 根据申请ID查询详情
     * 注：SQL 逻辑在 XML 映射文件中定义
     */
    BorrowApplyListVO selectApplyDetailById(@Param("applyId") Long applyId);

    /**
     * 查询重复申请数量（同一物品+同一用户+待审核/已同意）
     * 注：SQL 逻辑在 XML 映射文件中定义
     */
    long countRepeatApplications(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId,
            @Param("status1") Integer status1,
            @Param("status2") Integer status2
    );
}