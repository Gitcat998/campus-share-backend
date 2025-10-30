<template>
  <div id="app">
    <Header />
    <el-main style="min-height: calc(100vh - 60px);">
      <router-view />
    </el-main>
  </div>
</template>

<script>
import Header from './components/layout/Header.vue'

export default {
  components: {
    Header
  },
  mounted() {
    // 页面加载时，若有Token则获取用户信息
    if (this.$store.state.user.token) {
      this.$store.dispatch('user/getInfo').catch(() => {
        // Token无效时清除
        this.$store.dispatch('user/logout')
      })
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>