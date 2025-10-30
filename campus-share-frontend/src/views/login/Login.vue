<template>
  <div class="login-container">
    <el-card class="login-card" header="用户登录">
      <el-form ref="loginForm" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="account">
          <el-input v-model="form.account" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号账号？<a href="#/register">立即注册</a>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  data() {
    return {
      form: {
        account: '', // 与后端参数名一致（后端用account）
        password: ''
      },
      rules: {
        account: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    ...mapActions('user', ['login']),
    async handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          try {
            console.log('登录参数：', this.form);

            // 1. 调用登录接口获取Token
            const loginRes = await this.login(this.form);
            console.log('登录接口返回：', loginRes);

            // 2. 必须等待用户信息获取并保存完成
            const userInfo = await this.$store.dispatch('user/getInfo');
            console.log('登录后获取的用户信息：', userInfo);

            // 3. 确认用户信息已存入Vuex
            if (!this.$store.state.user.userInfo) {
              throw new Error('用户信息未正确加载');
            }

            this.$message.success('登录成功');
            // 优先跳转登录前页面，否则去物品列表
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
.register-link a {
  color: #409eff;
  text-decoration: none;
}
</style>