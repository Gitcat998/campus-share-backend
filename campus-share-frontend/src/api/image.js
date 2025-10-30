import request from '../utils/request'

/**
 * 上传图片（通用接口）
 * @param {FormData} formData - 图片表单数据
 * @returns {Promise} - 后端返回 { imageId, imageUrl }
 */
export function uploadImage(formData) {
    return request({
        url: '/images/upload',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data' // 图片上传需指定类型
        }
    })
}

/**
 * 上传用户头像
 * @param {FormData} formData - 头像表单数据
 * @returns {Promise}
 */
export function uploadAvatar(formData) {
    return request({
        url: '/images/upload/avatar',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}