import request from '@/utils/request'

// 管理员审核相关API
export function getPendingItems(params) {
    return request({
        url: '/items',
        method: 'get',
        params: {
            ...params,
            status: 0 // 强制筛选待审核状态
        }
    })
}

// 审核物品 - 修复：不需要请求体，状态通过查询参数传递
export function auditItem(itemId, status) {
    return request({
        url: `/items/${itemId}/audit`,
        method: 'put',
        params: { status } // 通过查询参数传递状态
    })
}

// 数据统计概览
export function getStatsOverview() {
    return request({
        url: '/stats/overview',
        method: 'get'
    })
}