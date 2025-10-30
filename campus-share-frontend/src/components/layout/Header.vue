<template>
  <el-header class="header">
    <!-- Logo 保持在左侧 -->
    <div class="logo">CampusShare 校园闲置</div>

    <!-- 菜单容器：通过 flex 推到右侧 -->
    <div class="menu-wrapper">
      <el-menu
          ref="mainMenu"
          class="main-menu"
          :default-active="activePath"
          mode="horizontal"
          @select="handleSelect"
          router
      >
        <!-- 1. 公共功能区 -->
        <el-menu-item index="/items">物品列表</el-menu-item>

        <!-- 2. 用户专属区 -->
        <template v-if="isLogin">
          <el-menu-item index="/item/publish">发布物品</el-menu-item>

          <el-submenu index="/user-center" v-if="!isAdmin">
            <template #title>个人中心</template>
            <el-menu-item index="/user/profile">个人信息</el-menu-item>
            <el-menu-item index="/borrow-applies/my">我的申请</el-menu-item>
            <el-menu-item index="/user/borrow-records">我的借用记录</el-menu-item>
          </el-submenu>

          <el-submenu index="/admin-center" v-if="isAdmin">
            <template #title>管理员中心</template>
            <el-menu-item index="/admin/audit">物品审核</el-menu-item>
            <el-menu-item index="/admin/stats">数据统计</el-menu-item>
          </el-submenu>
        </template>

        <template v-else>
          <el-menu-item index="/login">登录</el-menu-item>
          <el-menu-item index="/register">注册</el-menu-item>
        </template>

        <!-- 3. 操作区 -->
        <div class="menu-actions">
          <el-menu-item
              v-if="isLogin"
              @click="handleLogout"
              style="cursor: pointer;"
          >
            退出
          </el-menu-item>
        </div>
      </el-menu>
    </div>
  </el-header>
</template>

<script>
import { mapState } from 'vuex'
import { removeToken } from '@/utils/auth'

export default {
  name: 'AppHeader',
  data() {
    return {
      // 已移除debugMode相关配置
    }
  },
  computed: {
    ...mapState('user', ['userInfo', 'token']),
    activePath() {
      const path = this.$route.path
      const userSubPaths = new Set(['/user/profile', '/borrow-applies/my', '/user/borrow-records'])
      const adminSubPaths = new Set(['/admin/audit', '/admin/stats'])
      return userSubPaths.has(path) ? '/user-center' :
          adminSubPaths.has(path) ? '/admin-center' : path
    },
    isLogin() {
      return !!this.token
    },
    isAdmin() {
      return this.userInfo?.roleName === '管理员'
    }
  },
  watch: {
    token() {
      this.$forceUpdate()
    },
    $route: {
      immediate: true,
      handler() {
        this.$nextTick(() => {
          this.$refs.mainMenu?.updateActiveName()
        })
      }
    }
  },
  methods: {
    handleSelect(path) {
      console.log('菜单选择:', path)
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        removeToken()
        this.$store.commit('user/RESET_STATE')
        await this.$nextTick()
        this.$router.push('/login')
        this.$message.success('退出成功')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  background-color: #409eff;
  padding: 0 20px;
  height: 60px;
  width: 100%;
  box-sizing: border-box;
  justify-content: space-between;
}

.logo {
  color: white;
  font-size: 20px;
  font-weight: bold;
  white-space: nowrap;
  margin-right: 20px;
}

.menu-wrapper {
  margin-left: auto;
  display: flex;
  align-items: center;
  width: auto;
}

.main-menu {
  background-color: transparent !important;
  border-bottom: none !important;
  display: flex;
  align-items: center;
}

.menu-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
}

.main-menu .el-menu-item {
  color: white !important;
  font-size: 14px;
  padding: 0 15px !important;
}

:deep(.main-menu .el-submenu) {
  margin: 0 5px;
}

@media screen and (max-width: 992px) {
  .logo {
    font-size: 16px;
    margin-right: 10px;
  }
  .main-menu .el-menu-item {
    font-size: 13px;
    padding: 0 10px !important;
  }
}

@media screen and (max-width: 768px) {
  .main-menu .el-menu-item {
    font-size: 12px;
    padding: 0 8px !important;
  }
}

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
}
</style>