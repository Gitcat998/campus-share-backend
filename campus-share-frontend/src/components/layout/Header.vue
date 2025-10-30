<template>
  <el-header class="header">
    <div class="logo">CampusShare 校园闲置</div>
    <el-menu
        class="main-menu"
        :default-active="activePath"
        mode="horizontal"
        @select="handleSelect"
        router
    >
      <!-- 调试信息 -->
      <el-menu-item v-if="debugMode" style="color: #ff6b6b !important;">
        路径: {{ activePath }} | 登录: {{ isLogin }} | 管理员: {{ isAdmin }}
      </el-menu-item>

      <!-- 1. 公共入口：物品列表 -->
      <el-menu-item index="/items">物品列表</el-menu-item>

      <!-- 2. 已登录用户入口：发布物品 -->
      <el-menu-item index="/item/publish" v-if="isLogin">发布物品</el-menu-item>

      <!-- 3. 个人中心下拉菜单 -->
      <el-submenu index="/user-center" v-if="isLogin && !isAdmin">
        <template #title>个人中心</template>
        <el-menu-item index="/user/profile">个人信息</el-menu-item>
        <el-menu-item index="/borrow-applies/my">我的申请</el-menu-item>
        <el-menu-item index="/user/borrow-records">我的借用记录</el-menu-item>
      </el-submenu>

      <!-- 4. 管理员下拉菜单 -->
      <el-submenu index="/admin-center" v-if="isAdmin">
        <template #title>管理员中心</template>
        <el-menu-item index="/admin/audit">物品审核</el-menu-item>
        <el-menu-item index="/admin/stats">数据统计</el-menu-item>
      </el-submenu>

      <!-- 5. 未登录入口 -->
      <el-menu-item index="/login" v-if="!isLogin">登录</el-menu-item>
      <el-menu-item index="/register" v-if="!isLogin">注册</el-menu-item>

      <!-- 6. 退出 -->
      <el-menu-item v-if="isLogin" @click="handleLogout" style="cursor: pointer;">退出</el-menu-item>
    </el-menu>
  </el-header>
</template>

<script>
import { mapState } from 'vuex'
import { removeToken } from '@/utils/auth'

export default {
  name: 'AppHeader',
  data() {
    return {
      debugMode: true // 生产环境设为false
    }
  },
  computed: {
    ...mapState('user', ['userInfo', 'token']), // 从Vuex获取token和用户信息
    activePath() {
      const path = this.$route.path
      // 个人中心子菜单匹配
      if (['/user/profile', '/borrow-applies/my', '/user/borrow-records'].includes(path)) {
        return '/user-center'
      }
      // 管理员中心子菜单匹配
      if (['/admin/audit', '/admin/stats'].includes(path)) {
        return '/admin-center'
      }
      return path
    },
    isLogin() {
      return !!this.token // 基于Vuex的token判断登录状态
    },
    isAdmin() {
      return this.userInfo && this.userInfo.roleName === '管理员'
    }
  },
  mounted() {
    console.log('=== 导航组件挂载完成 ===')
    console.log('当前路由:', this.$route)
    console.log('用户状态:', this.$store.state.user)
  },
  methods: {
    handleSelect(path) {
      console.log('菜单点击:', path)
      // 路由跳转由el-menu的router模式自动处理
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        removeToken()
        this.$store.commit('user/RESET_STATE')
        this.$message.success('退出成功')
        // 跳转登录页并强制刷新组件
        this.$router.push('/login').then(() => {
          this.$forceUpdate()
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #409eff;
  padding: 0 20px;
  height: 60px;
}
.logo {
  color: white;
  font-size: 20px;
  font-weight: bold;
}

.main-menu {
  background-color: transparent !important;
  border-bottom: none !important;
}

.main-menu .el-menu-item {
  color: white !important;
  font-size: 14px;
}

/* 兼容不同Vue版本的样式穿透 */
:deep(.main-menu .el-menu-item:not(.is-disabled):hover),
:deep(.main-menu .el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.2) !important;
  color: white !important;
}

:deep(.main-menu .el-submenu__title) {
  color: white !important;
}
:deep(.main-menu .el-submenu__title:hover) {
  background-color: rgba(255, 255, 255, 0.2) !important;
}

:deep(.main-menu .el-submenu__popper) {
  margin-top: 0 !important;
  background-color: #409eff !important;
  border: none !important;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2) !important;
}

:deep(.main-menu .el-submenu__popper .el-menu-item) {
  color: white !important;
  background-color: transparent !important;
  height: 40px !important;
  line-height: 40px !important;
  padding-left: 30px !important;
}

:deep(.main-menu .el-submenu__popper .el-menu-item:hover),
:deep(.main-menu .el-submenu__popper .el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.3) !important;
  color: white !important;
}
</style>