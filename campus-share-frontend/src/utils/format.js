import dayjs from 'dayjs'

/**
 * 格式化时间，兼容 null/undefined 和无效时间
 * @param {Date|string|null} time - 时间对象、字符串或 null
 * @returns {string} 格式化后的时间（yyyy-MM-dd HH:mm:ss）或 '-'
 */
export function formatTime(time) {
    // 处理 null/undefined/空字符串
    if (!time || time === 'null' || time === 'undefined') return '-'
    // 处理无效时间格式
    if (!dayjs(time).isValid()) return '-'
    // 正常格式化时间
    return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 计算两个时间之间的天数差
 * @param {Date|string} start - 开始时间
 * @param {Date|string} end - 结束时间
 * @returns {number} 天数差（无效时间返回 0）
 */
export function getDayDiff(start, end) {
    if (!start || !end || !dayjs(start).isValid() || !dayjs(end).isValid()) {
        return 0
    }
    return dayjs(end).diff(dayjs(start), 'day', true)
}