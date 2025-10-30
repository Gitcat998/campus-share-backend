// router/index.js
import Vue from 'vue'
import Router from 'vue-router'
import { getToken } from '../utils/auth'
import { Message } from 'element-ui'
import store from '../store'

Vue.use(Router)

// 公共路由（无需登录）
const publicRoutes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/login/Login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/register/Register.vue')
    },
    {
        path: '/',
        redirect: '/items'
    },
    {
        path: '/items',
        name: 'ItemList',
        component: () => import('../views/item/ItemList.vue'),
        meta: { title: '物品列表' }
    },
    {
        path: '/items/:id',
        name: 'ItemDetail',
        component: () => import('../views/item/ItemDetail.vue'),
        meta: { title: '物品详情' }
    }
]

// 私有路由（需登录）
const privateRoutes = [
    {
        path: '/user/profile',
        name: 'Profile',
        component: () => import('../views/user/Profile.vue'),
        meta: { title: '个人信息' }
    },
    {
        path: '/item/publish',
        name: 'ItemPublish',
        component: () => import('../views/item/ItemPublish.vue'),
        meta: { title: '发布物品' }
    },
    {
        path: '/borrow/apply/:itemId',
        name: 'BorrowApply',
        component: () => import('../views/borrow/Apply.vue'),
        meta: { title: '申请借用' }
    },
    {
        path: '/borrow-applies/my',
        name: 'MyBorrowApplies',
        component: () => import('../views/user/MyBorrowApplies.vue'),
        meta: { title: '我的申请列表' }
    },
    {
        path: '/user/borrow-records',
        name: 'MyBorrowRecords',
        component: () => import('../views/user/MyBorrowRecords.vue'),
        meta: { title: '我的借用记录' }
    }

]

// 管理员路由
const adminRoutes = [
    {
        path: '/admin/audit',
        name: 'ItemAudit',
        component: () => import('../views/admin/ItemAudit.vue'),
        meta: { title: '物品审核', requireAdmin: true }
    },

    {
        path: '/admin/borrow-apply-audit',  // 新增路由
        name: 'BorrowApplyAudit',
        component: () => import('../views/admin/BorrowApplyAudit.vue'),  // 对应组件路径
        meta: { title: '借用申请审核', requireAdmin: true }
    },

    {
        path: '/admin/stats',
        name: 'AdminStats',
        component: () => import('../views/admin/Stats.vue'),
        meta: { title: '数据统计', requireAdmin: true }
    }
]

// 创建路由实例 - 修复：初始包含所有路由
const router = new Router({
    mode: 'history',
    routes: [
        ...publicRoutes,
        ...privateRoutes,
        ...adminRoutes  // 直接包含管理员路由，简化调试
    ]
})

// 路由守卫 - 简化版本
router.beforeEach((to, from, next) => {
    console.log('🚀 路由守卫: ', to.path)
    const token = getToken()
    const isLogin = !!token

    // 1. 未登录处理
    if (!isLogin) {
        const isPublic = publicRoutes.some(route => route.path === to.path)
        if (isPublic) {
            next()
        } else {
            Message.warning('请先登录')
            next('/login')
        }
        return
    }

    // 2. 管理员权限检查
    if (to.matched.some(record => record.meta.requireAdmin)) {
        const userInfo = store.state.user.userInfo
        const role = userInfo ? userInfo.roleName : null

        console.log('👤 管理员权限检查 - 角色:', role)

        if (!role || role !== '管理员') {
            Message.error('无管理员权限')
            next('/items')
            return
        }
    }

    next()
})

export default router