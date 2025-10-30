import Cookies from 'js-cookie'

const TokenKey = 'campus_share_token'

// 获取Token
export function getToken() {
    return Cookies.get(TokenKey)
}

// 存储Token（有效期1天）
export function setToken(token) {
    return Cookies.set(TokenKey, token, { expires: 1 })
}

// 删除Token
export function removeToken() {
    return Cookies.remove(TokenKey)
}