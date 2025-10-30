<template>
  <div class="register-container">
    <el-card class="register-card" header="用户注册">
      <el-form ref="registerForm" :model="form" :rules="rules" label-width="80px">
        <!-- 学号输入项（必填） -->
        <el-form-item label="学号" prop="studentId">
          <el-input
              v-model="form.studentId"
              placeholder="请输入学号（数字）"
              prefix-icon="el-icon-school"
              clearable
          ></el-input>
        </el-form-item>

        <!-- 用户名 -->
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名（3-20位）"
              prefix-icon="el-icon-user"
              clearable
          ></el-input>
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（6-20位）"
              prefix-icon="el-icon-lock"
              clearable
              show-password
          ></el-input>
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="el-icon-lock"
              clearable
              show-password
          ></el-input>
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item label="手机号" prop="phone">
          <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              prefix-icon="el-icon-phone"
              clearable
          ></el-input>
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              @click="handleRegister"
              style="width: 100%"
              :loading="loading"
          >
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>

        <!-- 跳转登录 -->
        <div class="login-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { register } from '@/api/user' // 导入注册API

export default {
  name: 'Register',
  data() {
    // 自定义校验：确认密码与密码一致
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    return {
      loading: false, // 加载状态
      form: {
        studentId: '', // 学号（与后端DTO字段一致）
        username: '',  // 用户名
        password: '',  // 密码
        confirmPassword: '', // 确认密码
        phone: ''      // 手机号
      },
      // 表单校验规则（与后端保持一致）
      rules: {
        studentId: [
          { required: true, message: '学号不能为空', trigger: 'blur' },
          { pattern: /^\d+$/, message: '学号必须为纯数字', trigger: 'blur' }
        ],
        username: [
          { required: true, message: '用户名不能为空', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '密码不能为空', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '手机号不能为空', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    async handleRegister() {
      // 表单验证
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          this.loading = true
          try {
            // 1. 整理参数（移除confirmPassword，仅传递后端需要的字段）
            const params = {
              studentId: this.form.studentId,
              username: this.form.username,
              password: this.form.password,
              phone: this.form.phone
            }

            // 2. 调用注册接口
            console.log('注册参数:', params)
            const res = await register(params)

            // 3. 处理响应
            if (res.code === 200) {
              this.$message.success('注册成功，请登录')
              // 跳转到登录页，并携带用户名（方便用户输入）
              this.$router.push({ path: '/login', query: { username: this.form.username } })
            } else {
              // 后端返回的业务错误（如学号已存在）
              this.$message.error(res.msg || '注册失败')
            }
          } catch (error) {
            // 网络错误或接口异常
            console.error('注册请求失败:', error)
            this.$message.error('网络错误，请重试')
          } finally {
            this.loading = false // 关闭加载状态
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.register-card {
  width: 420px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.login-link {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>