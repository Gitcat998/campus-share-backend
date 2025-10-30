package com.campus.campussharebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.campussharebackend.dto.ReturnConfirmDTO;
import com.campus.campussharebackend.entity.BorrowApply;
import com.campus.campussharebackend.entity.BorrowRecord;
import com.campus.campussharebackend.entity.Item;
import com.campus.campussharebackend.entity.User;
import com.campus.campussharebackend.exception.BusinessException;
import com.campus.campussharebackend.mapper.BorrowApplyMapper;
import com.campus.campussharebackend.mapper.BorrowRecordMapper;
import com.campus.campussharebackend.mapper.ItemMapper;
import com.campus.campussharebackend.mapper.UserMapper;
import com.campus.campussharebackend.service.BorrowRecordService;
import com.campus.campussharebackend.service.MessageService;
import com.campus.campussharebackend.vo.BorrowRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

    @Resource
    private BorrowRecordMapper borrowRecordMapper;

    @Resource
    private BorrowApplyMapper borrowApplyMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageService messageService;

    // 记录状态：0-借用中，1-已归还，2-超期未还
    private static final Integer RECORD_BORROWING = 0;
    private static final Integer RECORD_RETURNED = 1;
    private static final Integer RECORD_OVERDUE = 2;

    // 物品状态：1-可借用，2-已借出
    private static final Integer ITEM_AVAILABLE = 1;
    private static final Integer ITEM_BORROWED = 2;

    // 消息类型：1-归还通知
    private static final Integer MESSAGE_TYPE_RETURN = 1;


    @Override
    @Transactional
    public void confirmReturn(Long recordId, ReturnConfirmDTO dto, Long adminId) {
        // 1. 校验记录存在且状态为"借用中"
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("借用记录不存在");
        }
        if (!record.getStatus().equals(RECORD_BORROWING)) {
            throw new BusinessException("记录状态异常（当前状态：" + record.getStatus() + "）");
        }

        // 2. 校验审核人是管理员（优化：通过角色ID判断更严谨）
        User admin = userMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        // 假设角色表中管理员角色ID为2，用角色ID判断更可靠
        if (!admin.getRoleId().equals(2L)) {
            throw new BusinessException("无确认权限（需管理员）");
        }

        // 3. 校验时间合理性（实际归还时间 >= 借用开始时间）
        if (dto.getReturnTime().isBefore(record.getStartTime())) {
            throw new BusinessException("实际归还时间不能早于借用时间");
        }

        // 4. 更新记录状态和实际归还时间
        record.setReturnTime(dto.getReturnTime());
        // 判断是否超期（实际归还时间 > 计划归还时间）
        record.setStatus(dto.getReturnTime().isAfter(record.getEndTime()) ? RECORD_OVERDUE : RECORD_RETURNED);
        record.setUpdateTime(LocalDateTime.now());
        borrowRecordMapper.updateById(record);

        // 5. 更新物品状态为"可借用"
        BorrowApply apply = borrowApplyMapper.selectById(record.getApplyId());
        if (apply == null) {
            throw new BusinessException("关联申请记录不存在");
        }
        Item item = itemMapper.selectById(apply.getItemId());
        if (item == null) {
            throw new BusinessException("关联物品不存在");
        }
        item.setStatus(ITEM_AVAILABLE);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(item);

        // 6. 发送"物品已归还"消息给物品发布者（item的创建者）
        messageService.createMessage(
                item.getUserId(), // 接收人：物品发布者ID
                "物品已归还",      // 标题
                "您的物品《" + item.getName() + "》已被归还，实际归还时间：" + dto.getReturnTime(), // 内容
                MESSAGE_TYPE_RETURN // 类型：归还通知
        );
    }


    @Override
    public IPage<BorrowRecordVO> queryRecordList(Integer pageNum, Integer pageSize, Long userId) {
        IPage<BorrowRecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<BorrowRecord> queryWrapper = new QueryWrapper<>();

        // 关键修复：指定 user_id 来自 borrow_apply 表（别名 ba），与 XML 中表别名一致
        if (userId != null) {
            queryWrapper.eq("ba.user_id", userId); // 用 ba. 明确表归属
        }

        // 排序字段指定表别名 br（borrow_record表），避免歧义
        queryWrapper.orderByDesc("br.create_time");

        IPage<BorrowRecordVO> recordPage = borrowRecordMapper.selectRecordList(page, queryWrapper);

        // 转换状态码为文本
        recordPage.getRecords().forEach(vo -> {
            vo.setStatusText(convertRecordStatus(vo.getStatus()));
        });

        return recordPage;
    }


    // 记录状态转文本
    private String convertRecordStatus(Integer status) {
        switch (status) {
            case 0: return "借用中";
            case 1: return "已归还";
            case 2: return "超期未还";
            default: return "未知状态";
        }
    }
}