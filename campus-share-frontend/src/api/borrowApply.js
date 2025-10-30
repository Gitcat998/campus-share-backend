// src/api/borrowApply.js
import request from '../utils/request'

/**
 * 提交借用申请（用户端）
 */
export function submitBorrowApply(data) {
    return request({
        url: '/borrows/apply', // 匹配后端 @PostMapping("/apply")
        method: 'post',
        data
    })
}

/**
 * 获取我的借用申请列表（用户端）
 */
export function getMyBorrowApplies(params = {}) {
    const defaultParams = { pageNum: 1, pageSize: 10 }
    return request({
        url: '/borrows/applies', // 匹配后端 @GetMapping("/applies")
        method: 'get',
        params: { ...defaultParams, ...params }
    })
}

/**
 * 取消借用申请（用户端）
 */
export function cancelApply(applyId) {
    return request({
        url: `/borrow-applies/${applyId}`,
        method: 'delete'
    })
}

/**
 * 获取我的借用记录（用户端）
 */
export function getMyBorrowRecords(params = {}) {
    const defaultParams = { pageNum: 1, pageSize: 10 }
    return request({
        url: '/borrows/records', // 匹配后端 @GetMapping("/records")
        method: 'get',
        params: { ...defaultParams, ...params }
    })
}

/**
 * 确认归还物品（管理员）
 */
export function confirmReturn(recordId, data) {
    return request({
        url: `/borrows/records/${recordId}/return`, // 匹配后端 @PutMapping("/records/{recordId}/return")
        method: 'put',
        data
    })
}

// 管理员查询所有借用申请（适配后端接口）
export function queryBorrowApplies(params = {}) {
    const defaultParams = { pageNum: 1, pageSize: 10 }
    return request({
        url: '/borrows/applies', // 后端管理员和用户共用此接口，通过权限区分数据
        method: 'get',
        params: { ...defaultParams, ...params }
    })
}

// 管理员审核借用申请（适配后端接口）
export function auditBorrowApply(applyId, status, rejectReason) {
    // 添加参数验证
    if (!applyId || applyId === 'null' || applyId === 'undefined') {
        console.error('auditBorrowApply: applyId为空或无效:', applyId)
        return Promise.reject(new Error('申请ID不能为空'))
    }

    console.log('调用审核API，applyId:', applyId, 'status:', status)

    return request({
        url: `/borrows/applies/${applyId}/audit`,
        method: 'put',
        data: {
            status,
            rejectReason: rejectReason || ''
        }
    })
}