import { login, getInfo, logout } from '../../api/user'
import { setToken, removeToken, getToken } from '../../utils/auth'

const state = {
    token: getToken(), // 从本地存储初始化
    userInfo: null, // 用户信息
    role: '' // 角色：学生/管理员
}

const mutations = {
    SET_TOKEN: (state, token) => {
        state.token = token
    },
    SET_USER_INFO: (state, userInfo) => {
        state.userInfo = userInfo
        state.role = userInfo.roleName // 存储角色
    },
    CLEAR_USER: (state) => {
        state.token = null
        state.userInfo = null
        state.role = ''
    }
}

const actions = {
    // 登录
    login({ commit }, form) {
        return new Promise((resolve, reject) => {
            login(form).then(res => {
                console.log('后端返回的完整响应：', res);

                // 关键修正：先提取并保存Token，再resolve
                const { token } = res.data;
                console.log('从响应中提取的Token：', token);

                if (token) {
                    setToken(token);
                    commit('SET_TOKEN', token);
                    resolve(res); // 确保Token已保存后再返回
                } else {
                    reject(new Error('登录响应中未找到Token'));
                }
            }).catch(error => {
                console.error('登录接口调用失败：', error);
                console.error('错误响应状态：', error.response?.status);
                console.error('错误响应数据：', error.response?.data);
                reject(error);
            });
        });
    },

    // 获取用户信息（关键修正：确保正确提取用户信息）
    getInfo({ commit, state }) {
        return new Promise((resolve, reject) => {
            // 检查Token是否存在
            if (!state.token) {
                reject(new Error('Token不存在，无法获取用户信息'));
                return;
            }

            getInfo().then(res => {
                // 后端返回格式：{ code:200, data: { roleName: "管理员", ... } }
                const userInfo = res.data; // 从data中提取用户信息
                console.log('用户信息（含角色）：', userInfo);
                console.log('角色名称：', userInfo.roleName); // 应输出 "管理员" 或 "学生"
                if (!userInfo) {
                    reject(new Error('用户信息为空'));
                    return;
                }
                commit('SET_USER_INFO', userInfo); // 保存到Vuex
                resolve(userInfo); // 返回用户信息给组件
            }).catch(error => {
                console.error('获取用户信息失败：', error);
                reject(error);
            });
        });
    },

    // 退出登录
    logout({ commit }) {
        return new Promise(resolve => {
            removeToken()
            commit('CLEAR_USER')
            resolve()
        })
    }
}

export default {
    namespaced: true,
    state,
    mutations,
    actions
}