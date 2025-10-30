package com.campus.campussharebackend.service.impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.campussharebackend.dto.ApplyAuditDTO;
import com.campus.campussharebackend.dto.BorrowApplyDTO;
import com.campus.campussharebackend.entity.BorrowApply;
import com.campus.campussharebackend.entity.BorrowRecord;
import com.campus.campussharebackend.entity.Item;
import com.campus.campussharebackend.entity.User;
import com.campus.campussharebackend.exception.BusinessException;
import com.campus.campussharebackend.mapper.BorrowApplyMapper;
import com.campus.campussharebackend.mapper.BorrowRecordMapper;
import com.campus.campussharebackend.mapper.ItemMapper;
import com.campus.campussharebackend.mapper.UserMapper;
import com.campus.campussharebackend.service.BorrowApplyService;
import com.campus.campussharebackend.service.MessageService;
import com.campus.campussharebackend.vo.BorrowApplyListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class BorrowApplyServiceImpl extends ServiceImpl<BorrowApplyMapper, BorrowApply> implements BorrowApplyService {

    private static final Logger log = LoggerFactory.getLogger(BorrowApplyServiceImpl.class);

    @Resource
    private BorrowApplyMapper borrowApplyMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private BorrowRecordMapper borrowRecordMapper;

    // 注入消息服务（用于生成通知）
    @Resource
    private MessageService messageService;

    // 申请状态：0-待审核，1-已同意，2-已拒绝
    private static final Integer APPLY_PENDING = 0;
    private static final Integer APPLY_APPROVED = 1;
    private static final Integer APPLY_REJECTED = 2;

    // 物品状态：1-可借用，2-已借出
    private static final Integer ITEM_AVAILABLE = 1;
    private static final Integer ITEM_BORROWED = 2;

    // 消息类型：0-借用通知
    private static final Integer MESSAGE_TYPE_BORROW = 0;


    @Override
    @Transactional
    public Long submitApply(BorrowApplyDTO dto, Long userId) {
        // 1. 校验物品存在且状态为"可借用"
        Item item = itemMapper.selectById(dto.getItemId());
        if (item == null || item.getIsDelete() == 1) {
            throw new BusinessException("物品不存在");
        }
        if (!item.getStatus().equals(ITEM_AVAILABLE)) {
            throw new BusinessException("物品当前不可借用（状态：" + item.getStatus() + "）");
        }

        // 2. 校验申请人存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("申请人不存在");
        }

        // 3. 校验时间合理性（期望归还时间 >= 期望借用时间）
        if (dto.getExpectedReturnTime().isBefore(dto.getExpectedBorrowTime())) {
            throw new BusinessException("期望归还时间不能早于借用时间");
        }

        // 4. 校验是否重复申请（同一物品+同一用户+待审核/已同意的申请）
        // 改用SQL条件查询，避免创建QueryWrapper
        long repeatCount = borrowApplyMapper.countRepeatApplications(
                dto.getItemId(), userId, APPLY_PENDING, APPLY_APPROVED);
        if (repeatCount > 0) {
            throw new BusinessException("已提交该物品的借用申请，请勿重复提交");
        }

        // 5. 保存申请
        BorrowApply apply = new BorrowApply();
        apply.setItemId(dto.getItemId());
        apply.setUserId(userId);
        apply.setExpectedBorrowTime(dto.getExpectedBorrowTime());
        apply.setExpectedReturnTime(dto.getExpectedReturnTime());
        apply.setStatus(APPLY_PENDING); // 初始状态：待审核
        apply.setApplyTime(LocalDateTime.now());
        apply.setCreateTime(LocalDateTime.now());
        borrowApplyMapper.insert(apply);

        return apply.getId();
    }


    @Override
    @Transactional
    public void auditApply(Long applyId, ApplyAuditDTO auditDTO, Long adminId) {
        // 1. 校验申请存在且状态为"待审核"
        BorrowApply apply = borrowApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("申请不存在");
        }
        if (!apply.getStatus().equals(APPLY_PENDING)) {
            throw new BusinessException("申请已审核，无需重复操作");
        }

        // 2. 校验审核人是管理员
        User admin = userMapper.selectById(adminId);
        if (admin == null || admin.getRoleId() != 2L) { // 管理员role_id=2
            throw new BusinessException("无审核权限（需管理员）");
        }

        // 3. 处理审核结果
        apply.setStatus(auditDTO.getStatus());
        apply.setUpdateTime(LocalDateTime.now());

        // 获取物品信息（用于消息内容）
        Item item = itemMapper.selectById(apply.getItemId());
        if (item == null) {
            throw new BusinessException("关联物品不存在");
        }

        if (auditDTO.getStatus().equals(APPLY_REJECTED)) {
            // 3.1 拒绝申请：记录拒绝原因 + 发送拒绝消息
            if (auditDTO.getRejectReason() == null || auditDTO.getRejectReason().trim().isEmpty()) {
                throw new BusinessException("拒绝申请需填写原因");
            }
            apply.setRejectReason(auditDTO.getRejectReason());
            borrowApplyMapper.updateById(apply);

            // 发送"申请被拒绝"消息给申请人
            messageService.createMessage(
                    apply.getUserId(), // 接收人：申请人ID
                    "借用申请已拒绝",  // 标题
                    "您申请的物品《" + item.getName() + "》未通过审核，原因：" + auditDTO.getRejectReason(), // 内容
                    MESSAGE_TYPE_BORROW // 类型：借用通知
            );

        } else if (auditDTO.getStatus().equals(APPLY_APPROVED)) {
            // 3.2 同意申请：生成借用记录 + 更新物品状态 + 发送同意消息
            apply.setRejectReason(""); // 清空拒绝原因
            borrowApplyMapper.updateById(apply);

            // 生成借用记录
            BorrowRecord record = new BorrowRecord();
            record.setApplyId(applyId);
            record.setStartTime(apply.getExpectedBorrowTime()); // 实际借用时间=期望时间
            record.setEndTime(apply.getExpectedReturnTime());    // 计划归还时间=期望时间
            record.setStatus(0); // 状态：借用中
            record.setCreateTime(LocalDateTime.now());
            borrowRecordMapper.insert(record);

            // 更新物品状态为"已借出"
            item.setStatus(ITEM_BORROWED);
            item.setUpdateTime(LocalDateTime.now());
            itemMapper.updateById(item);

            // 发送"申请被同意"消息给申请人
            messageService.createMessage(
                    apply.getUserId(), // 接收人：申请人ID
                    "借用申请已同意",  // 标题
                    "您申请的物品《" + item.getName() + "》已通过审核，可在" + apply.getExpectedBorrowTime() + "前往领取", // 内容
                    MESSAGE_TYPE_BORROW // 类型：借用通知
            );
        }
    }


    @Override
    public IPage<BorrowApplyListVO> queryApplyList(
            Integer pageNum,
            Integer pageSize,
            Long userId,
            Integer isAdmin,
            Integer status) {

        // 添加详细日志
        log.info("=== 查询申请列表 Service ===");
        log.info("参数 - pageNum: {}, pageSize: {}, userId: {}, isAdmin: {}, status: {}",
                pageNum, pageSize, userId, isAdmin, status);

        Page<BorrowApplyListVO> page = new Page<>(pageNum, pageSize);

        try {
            // 调用COUNT方法时传递status
            Long total = borrowApplyMapper.selectApplyList_COUNT(userId, isAdmin, status);
            page.setTotal(total);
            log.info("COUNT查询结果 - 总记录数: {}", total);

            // 调用主查询方法时传递status
            IPage<BorrowApplyListVO> applyPage = borrowApplyMapper.selectApplyList(
                    page, userId, isAdmin, status);
            log.info("主查询结果 - 实际记录数: {}", applyPage.getRecords().size());

            page.setRecords(applyPage.getRecords());

            // 转换状态文本并记录详细信息
            page.getRecords().forEach(vo -> {
                vo.setStatusText(convertApplyStatus(vo.getStatus()));
                // 修复：使用 getUsername() 而不是 getUserName()
                log.info("申请详情 - ID: {}, 物品: {}, 用户: {}, 状态: {}",
                        vo.getId(), vo.getItemName(), vo.getUsername(), vo.getStatus());
            });

        } catch (Exception e) {
            log.error("查询申请列表异常: {}", e.getMessage(), e);
        }

        log.info("=== 查询申请列表 Service 结束 ===");
        return page;
    }


    // 申请状态转文本（将数字状态码转换为中文描述）
    private String convertApplyStatus(Integer status) {
        switch (status) {
            case 0: return "待审核";
            case 1: return "已同意";
            case 2: return "已拒绝";
            default: return "未知状态";
        }
    }
}