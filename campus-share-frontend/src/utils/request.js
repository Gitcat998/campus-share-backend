import axios from 'axios'
import { Message } from 'element-ui'
import { getToken } from './auth'
import { removeToken } from './auth'

// 创建实例
const service = axios.create({
    baseURL: 'http://localhost:8080/api/v1', // 后端接口基础路径
    timeout: 10000, // 延长超时时间（文件上传可能较慢）
    headers: {
        'Content-Type': 'application/json;charset=utf-8' // 默认JSON格式
    }
})

// 请求拦截器：添加 Token
service.interceptors.request.use(
    config => {
        const token = getToken()//token会从Cookie中获取，与setToken()匹配
        if (token) {
            config.headers.Authorization = `Bearer ${token}` // JWT 标准格式
        }
        return config
    },
    error => {
        Message.error('请求发送失败，请重试')
        return Promise.reject(error)
    }
)

// 响应拦截器：处理后端响应
service.interceptors.response.use(
    response => {
        const res = response.data
        // 后端统一响应格式：{ code, message, data }
        if (res.code !== 200) {
            // 特殊错误处理：Token 过期（根据后端状态码调整）
            if (res.code === 401) {
                Message.error('登录已过期，请重新登录')
                // 清除无效 Token 并跳转登录页
                removeToken()// 正确 Cookie 中清除 Token（与 setToken 对应）
                window.location.href = '/login'
            } else {
                Message.error(res.message || '操作失败')
            }
            return Promise.reject(res)
        }
        return res // 直接返回 data 字段
    },
    error => {
        // 细化错误提示
        if (error.message.includes('Network Error')) {
            Message.error('网络异常，请检查后端是否启动或接口地址是否正确')
        } else if (error.message.includes('timeout')) {
            Message.error('请求超时，请重试')
        } else if (error.response) {
            const status = error.response.status
            if (status === 404) {
                Message.error('请求的接口不存在')
            } else if (status === 500) {
                Message.error('服务器内部错误')
            } else {
                Message.error(`请求失败（${status}）`)
            }
        } else {
            Message.error('未知错误')
        }
        return Promise.reject(error)
    }
)

export default service