<template>
  <div class="login-container">
    <el-card class="login-card" header="用户登录">
      <el-form ref="loginForm" :model="form" :rules="rules" label-width="80px">
        <!-- 表单内容保持不变 -->
        <el-form-item label="用户名" prop="account">
          <el-input v-model="form.account" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form-item>

        <!-- 修复：用路由跳转代替锚点链接 -->
        <div class="register-link">
          还没有账号？
          <span class="register-btn" @click="goToRegister">立即注册</span>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import {mapActions} from 'vuex'

export default {
  data() {
    return {
      form: {
        account: '',
        password: ''
      },
      rules: {
        account: [{required: true, message: '请输入用户名', trigger: 'blur'}],
        password: [{required: true, message: '请输入密码', trigger: 'blur'}]
      }
    }
  },
  methods: {
    ...mapActions('user', ['login']),
    async handleLogin() {
      // 原有登录逻辑保持不变
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          try {
            console.log('登录参数：', this.form);
            const loginRes = await this.login(this.form);
            console.log('登录接口返回：', loginRes);
            const userInfo = await this.$store.dispatch('user/getInfo');
            console.log('登录后获取的用户信息：', userInfo);

            if (!this.$store.state.user.userInfo) {
              throw new Error('用户信息未正确加载');
            }

            this.$message.success('登录成功');
            const redirect = this.$route.query.redirect || '/items';
            await this.$router.push(redirect).catch(err => {
              if (!err.message.includes('Redirected when going')) throw err;
            });
          } catch (error) {
            console.error('登录失败:', error);
            this.$message.error('登录失败，请重试');
          }
        }
      });
    },
    // 新增：跳转到注册页的方法
    goToRegister() {
      this.$router.push('/register').catch(err => {
        // 捕获可能的路由错误（如注册页路由未定义）
        console.error('跳转注册页失败：', err);
        this.$message.error('注册页面不存在');
      });
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.login-card {
  width: 400px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-link {
  text-align: center;
  margin-top: 15px;
}

/* 修复：给按钮添加链接样式 */
.register-btn {
  color: #409eff;
  cursor: pointer;
  text-decoration: none;
}

.register-btn:hover {
  text-decoration: underline; /* 保持hover效果和原链接一致 */
}
</style>