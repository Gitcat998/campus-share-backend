import request from '../utils/request'

/**
 * 提交借用申请
 * @param {Object} data - 申请信息
 * @param {number} data.itemId - 物品ID
 * @param {string} data.startTime - 借用开始时间（yyyy-MM-dd HH:mm:ss）
 * @param {string} data.endTime - 借用结束时间（yyyy-MM-MM-dd HH:mm:ss）
 * @param {string} data.reason - 借用理由（必填，优化为非可选）
 * @returns {Promise}
 */
export function submitBorrowApply(data) {
    return request({
        url: '/borrows/apply',
        method: 'post',
        data
    })
}

/**
 * 获取我的借用申请列表
 * @param {Object} [params] - 筛选和分页参数
 * @param {number} [params.status] - 申请状态（0-待审核，1-已同意，2-已拒绝）
 * @param {number} [params.pageNum=1] - 页码（默认第一页）
 * @param {number} [params.pageSize=10] - 每页条数（默认10条）
 * @returns {Promise}
 */
export function getMyBorrowApplies(params = {}) {
    const defaultParams = { pageNum: 1, pageSize: 10 }
    return request({
        url: '/borrows/applies', // 接口路径正确，无需修改
        method: 'get',
        params: { ...defaultParams, ...params }
    })
}
/**
 * 取消借用申请（仅待审核状态可取消）
 * @param {number} applyId - 申请ID
 * @returns {Promise}
 */
export function cancelApply(applyId) {
    return request({
        url: `/borrow-applies/${applyId}`,
        method: 'delete'
    })
}

// 新增：获取我的借用记录（已通过的申请，进行中/已归还）
/**
 * 获取我的借用记录
 * @param {Object} [params] - 筛选参数
 * @param {number} [params.status] - 记录状态（1-进行中，2-已归还）
 * @param {number} [params.pageNum=1] - 页码
 * @param {number} [params.pageSize=10] - 每页条数
 * @returns {Promise}
 */
export function getMyBorrowRecords(params = {}) {
    const defaultParams = { pageNum: 1, pageSize: 10 }
    return request({
        url: '/borrows/records',
        method: 'get',
        params: { ...defaultParams, ...params }
    })
}

// 5. 确认归还物品（管理员，适配后端PUT /borrows/records/{recordId}/return）
export function confirmReturn(recordId, data) {
    return request({
        url: `/borrows/records/${recordId}/return`,
        method: 'put',
        data // { returnTime: '2025-10-30' }
    })
}