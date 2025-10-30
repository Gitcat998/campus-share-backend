import request from '../utils/request'

// 用户登录
export function login(data) {
    return request({
        url: '/users/login',
        method: 'post',
        data
    })
}

// 用户注册
export function register(data) {
    return request({
        url: '/users/register',
        method: 'post',
        data
    })
}

// 获取用户信息
export function getInfo() {
    return request({
        url: '/users/info',
        method: 'get'
    })
}

// 更新用户信息
export function updateUserInfo(data) {
    return request({
        url: '/users/info', // 拼接后：http://localhost:8080/api/v1/users/info
        method: 'put',
        data
    })
}