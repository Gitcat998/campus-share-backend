<template>
  <div class="profile-container">
    <el-card title="个人信息" class="profile-card">
      <el-row :gutter="30">
        <!-- 左侧头像区域 -->
        <el-col :span="6">
          <div class="avatar-section">
            <div class="avatar-container">
              <img
                  :src="userInfo.avatar || defaultAvatar"
                  alt="用户头像"
                  class="user-avatar"
              >
            </div>
            <!-- 头像上传组件 -->
            <el-upload
                class="avatar-uploader"
                action="/api/v1/users/upload-avatar"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :headers="{ 'Authorization': `Bearer ${getToken()}` }"
            >
              <el-button size="small" type="primary" style="margin-top: 15px;">
                更换头像
              </el-button>
            </el-upload>
          </div>
        </el-col>

        <!-- 右侧信息编辑区域 -->
        <el-col :span="18">
          <el-form
              ref="profileForm"
              :model="userInfo"
              :rules="formRules"
              label-width="120px"
              class="profile-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userInfo.username" disabled placeholder="用户名不可修改"></el-input>
            </el-form-item>

            <el-form-item label="昵称" prop="name">
              <el-input v-model="userInfo.name" placeholder="请输入昵称"></el-input>
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userInfo.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userInfo.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>

            <el-form-item label="个人简介">
              <el-input
                  v-model="userInfo.introduction"
                  type="textarea"
                  rows="4"
                  placeholder="请输入个人简介（选填）"
                  maxlength="200"
                  show-word-limit
              ></el-input>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitProfile">保存修改</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { getInfo, updateUserInfo } from '@/api/user'// 接口：获取/更新用户信息
import { getToken } from '@/utils/auth' // 获取Token
import defaultAvatar from '@/assets/images/default.png' // 默认头像

export default {
  name: 'Profile',
  data() {
    return {
      defaultAvatar,
      userInfo: {
        username: '',
        name: '',
        phone: '',
        email: '',
        avatar: '',
        introduction: ''
      },
      formRules: {
        name: [
          { required: true, message: '请输入昵称', trigger: 'blur' },
          { min: 2, max: 10, message: '昵称长度在2-10字之间', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  created() {
    this.loadUserInfo() // 加载用户信息
  },
  methods: {

    getToken() {
      return getToken() // 调用导入的工具函数
    },

    // 1. 加载用户信息
    async loadUserInfo() {
      try {
        this.loading = true
        const res = await getInfo()
        if (res.code === 200) {
          this.userInfo = res.data // 填充用户信息
        } else {
          this.$message.error('获取个人信息失败')
        }
      } catch (error) {
        console.error('加载个人信息异常：', error)
        this.$message.error('网络异常，请重试')
      } finally {
        this.loading = false
      }
    },

    // 2. 头像上传成功处理
    handleAvatarSuccess(response) {
      if (response.code === 200) {
        this.userInfo.avatar = response.data.avatarUrl // 更新头像URL
        this.$message.success('头像上传成功')
      } else {
        this.$message.error(response.message || '头像上传失败')
      }
    },

    // 3. 头像上传前校验
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        this.$message.error('头像必须是 JPG 或 PNG 格式')
      }
      if (!isLt2M) {
        this.$message.error('头像大小不能超过 2MB')
      }
      return isJPG && isLt2M
    },

    // 4. 提交个人信息修改
    async submitProfile() {
      this.$refs.profileForm.validate(async (valid) => {
        if (valid) {
          // 关键：打印提交给后端的完整数据
          console.log("前端提交的userInfo：", this.userInfo);

          try {
            this.loading = true
            const res = await updateUserInfo(this.userInfo)
            if (res.code === 200) {
              this.$message.success('信息更新成功')
              // 更新Vuex中的用户信息（保持全局同步）
              this.$store.commit('user/SET_USER_INFO', this.userInfo)
            } else {
              this.$message.error(res.message || '更新失败')
            }
          } catch (error) {
            console.error('更新信息异常：', error)
            this.$message.error('网络异常，请重试')
          } finally {
            this.loading = false
          }
        }
      })
    },

    // 5. 重置表单
    resetForm() {
      this.$refs.profileForm.resetFields()
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.profile-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.avatar-container {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #f0f0f0;
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-form {
  padding: 20px 0;
}
</style>