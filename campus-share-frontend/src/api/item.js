import request from '../utils/request'

/**
 * 获取物品列表（带分页和筛选）
 * @param {Object} params - 筛选参数
 */
export function getItemList(params) {
    return request({
        url: '/items', // 补充 /v1，匹配后端 /api/v1/items
        method: 'get',
        params
    })
}

/**
 * 获取物品分类列表（树形结构）
 */
export function getItemTypes() {
    return request({
        url: '/item-types/tree', // 补充 /v1，匹配后端 /api/v1/item-types/tree
        method: 'get'
    })
}

/**
 * 获取物品详情
 * @param {number} itemId - 物品ID
 */
export function getItemDetail(itemId) {
    return request({
        url: `/items/${itemId}`, // 补充 /v1，匹配后端 /api/v1/items/{itemId}
        method: 'get'
    })
}

/**
 * 发布新物品
 * @param {Object} data - 物品信息
 */
export function publishItem(data) {
    return request({
        url: '/items', // 补充 /v1，匹配后端 /api/v1/items
        method: 'post',
        data
    })
}

/**
 * 修正：更新物品状态（拆分审核/下架接口，与后端匹配）
 * 1. 审核通过：调用 /audit 接口
 * 2. 下架/拒绝：调用 /offline 接口
 */
// 审核通过物品（管理员）
export function auditItemPass(itemId) {
    return request({
        url: `/items/${itemId}/audit`, // 匹配后端 /api/v1/items/{itemId}/audit
        method: 'put'
    })
}

// 下架物品（发布者）或审核拒绝（管理员）
export function offlineItem(itemId) {
    return request({
        url: `/items/${itemId}/offline`, // 匹配后端 /api/v1/items/{itemId}/offline
        method: 'put'
    })
}

/**
 * 修正：获取物品评价列表（路径与后端评价接口匹配）
 * @param {number} itemId - 物品ID
 */
export function getComments(itemId, params = {}) {
    return request({
        url: `/comments/item/${itemId}`, // 匹配后端 /api/v1/comments/item/{itemId}
        method: 'get',
        params
    })
}

// 4. 条件查询物品列表（适配后端GET /items）
export function queryItems(params) {
    return request({
        url: '/items',
        method: 'get',
        params // 支持itemTypeId、name、status等参数
    })
}