import request from '../utils/request'

/**
 * 获取统计概览（包含所有统计数据）
 */
export function getStatsOverview() {
    return request({
        url: '/stats/overview', // 匹配文档
        method: 'get'
    })
}

/**
 * 获取物品状态统计
 */
export function getItemStatusCount() {
    return request({
        url: '/stats/items/status', // 匹配后端 /api/v1/stats/items/status
        method: 'get'
    })
}

/**
 * 获取分类物品数量统计
 */
export function getCategoryItemCount() {
    return request({
        url: '/stats/items/category', // 匹配后端 /api/v1/stats/items/category
        method: 'get'
    })
}

/**
 * 获取评价统计
 */
export function getCommentStats() {
    return request({
        url: '/stats/comments', // 匹配后端 /api/v1/stats/comments
        method: 'get'
    })
}