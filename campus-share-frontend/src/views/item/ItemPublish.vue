<template>
  <div class="item-publish-container">
    <el-card header="发布闲置物品">
      <el-form
          ref="publishForm"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="publish-form"
      >
        <!-- 物品名称 -->
        <el-form-item label="物品名称" prop="name">
          <el-input
              v-model="form.name"
              placeholder="请输入物品名称"
              maxlength="50"
              show-word-limit
          ></el-input>
        </el-form-item>

        <!-- 物品分类 -->
        <el-form-item label="物品分类" prop="itemTypeId">
          <el-select
              v-model.number="form.itemTypeId"
              placeholder="请选择分类"
              style="width: 100%"
          >
            <el-option
                v-for="type in itemTypes"
                :key="type.id"
                :label="type.typeName"
                :value="type.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <!-- 可借时长 -->
        <el-form-item label="可借时长（天）" prop="borrowDuration">
          <el-input-number
              v-model.number="form.borrowDuration"
              :min="1"
              :max="365"
              placeholder="请输入可借用的最大天数"
              style="width: 100%"
          ></el-input-number>
        </el-form-item>

        <!-- 物品描述 -->
        <el-form-item label="物品描述" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              rows="6"
              placeholder="请详细描述物品情况"
              maxlength="500"
              show-word-limit
          ></el-input>
        </el-form-item>

        <!-- 图片上传 -->
        <el-form-item
            label="物品图片"
            prop="images"
        >
          <el-upload
              action=""
              :on-change="handleImageChange"
              :file-list="fileList"
              :auto-upload="false"
              list-type="picture-card"
              :limit="5"
              :on-exceed="handleExceed"
              :on-remove="handleRemove"
              accept="image/*"
          >
            <i class="el-icon-plus"></i>
          </el-upload>
          <div class="form-hint">最多上传5张图片，第一张为封面图</div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              @click="handlePublish"
              :loading="publishing"
          >
            {{ publishing ? '发布中...' : '确认发布' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getItemTypes } from '../../api/item'
import { publishItem } from '../../api/item' // 假设有专门的发布API
import { getToken } from '../../utils/auth'

export default {
  name: 'ItemPublish',
  data() {
    return {
      form: {
        name: '',
        itemTypeId: null,
        borrowDuration: 7,
        description: ''
      },
      itemTypes: [],
      fileList: [], // 存储上传的图片文件
      publishing: false, // 发布状态
      rules: {
        name: [
          { required: true, message: '请输入物品名称', trigger: 'blur' },
          { min: 2, max: 50, message: '名称长度在2-50字之间', trigger: 'blur' }
        ],
        itemTypeId: [
          { required: true, message: '请选择物品分类', trigger: 'change' }
        ],
        borrowDuration: [
          { required: true, message: '请输入可借时长', trigger: 'blur' },
          { type: 'number', min: 1, message: '可借时长至少1天', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入物品描述', trigger: 'blur' },
          { min: 10, max: 500, message: '描述长度在10-500字之间', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadItemTypes()
  },
  methods: {
    // 加载物品分类
    async loadItemTypes() {
      try {
        const res = await getItemTypes()
        console.log('分类接口返回:', res)

        if (res.code === 200) {
          this.itemTypes = Array.isArray(res.data) ? res.data : []
          console.log('分类数据加载成功：', this.itemTypes)
        } else {
          this.$message.error('分类数据加载失败：' + (res.msg || '未知错误'))
        }
      } catch (error) {
        console.error('加载分类失败：', error)
        this.$message.error('加载分类失败，请刷新页面重试')
      }
    },

    // 处理图片上传变化
    handleImageChange(file, fileList) {
      console.log('图片上传变化:', fileList)
      this.fileList = fileList
    },

    // 处理图片移除
    handleRemove(file, fileList) {
      console.log('图片被移除:', file, fileList)
      this.fileList = fileList
    },

    // 图片超出限制提示
    handleExceed(files, fileList) {
      this.$message.warning(`最多只能上传5张图片，当前已选择${fileList.length}张`)
    },

    // 验证表单数据
    validateForm() {
      // 基本表单验证
      let valid = true
      this.$refs.publishForm.validate((isValid) => {
        valid = isValid
      })

      if (!valid) {
        this.$message.error('请完善表单信息')
        return false
      }

      // 图片验证
      if (this.fileList.length === 0) {
        this.$message.error('请至少上传1张图片')
        return false
      }

      // 文件大小验证（可选）
      const maxSize = 5 * 1024 * 1024 // 5MB
      for (const file of this.fileList) {
        if (file.raw && file.raw.size > maxSize) {
          this.$message.error(`图片 ${file.name} 大小不能超过5MB`)
          return false
        }
      }

      return true
    },

    // 发布物品
    async handlePublish() {
      console.log('开始发布物品...')

      // 验证表单
      if (!this.validateForm()) {
        return
      }

      this.publishing = true

      try {
        // 获取token
        const token = getToken()
        console.log('Token:', token ? '存在' : '不存在')

        if (!token) {
          this.$message.error('未检测到登录状态，请重新登录')
          this.$router.push('/login')
          return
        }

        // 构建FormData
        const formData = new FormData()

        // 添加文本字段 - 确保字段名与后端一致
        formData.append('name', this.form.name)
        formData.append('itemTypeId', this.form.itemTypeId.toString())
        formData.append('borrowDuration', this.form.borrowDuration.toString())
        formData.append('description', this.form.description)

        // 添加图片文件 - 确保字段名与后端一致
        this.fileList.forEach((file, index) => {
          formData.append('images', file.raw)
          console.log(`添加图片 ${index + 1}:`, file.name)
        })

        // 打印FormData内容（调试用）
        console.log('FormData内容:')
        for (let [key, value] of formData.entries()) {
          if (value instanceof File) {
            console.log(`${key}: File - ${value.name}, ${value.size} bytes`)
          } else {
            console.log(`${key}: ${value}`)
          }
        }

        // 发送请求
        console.log('发送发布请求...')
        const response = await this.$axios({
          url: '/items',
          method: 'post',
          data: formData,
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'multipart/form-data' // 重要：确保设置正确的Content-Type
          },
          timeout: 30000 // 30秒超时
        })

        console.log('发布请求完整响应:', response)

        // 处理响应
        if (response.code === 200) {
          this.$message.success('发布成功！等待管理员审核')

          // 跳转到物品详情页或物品列表
          if (response.data) {
            const itemId = response.data.id || response.data
            this.$router.push(`/items/${itemId}`)
          } else {
            this.$router.push('/items')
          }
        } else {
          console.error('发布失败，响应码:', response.code, '消息:', response.msg)
          this.$message.error('发布失败：' + (response.msg || '服务器处理失败'))
        }

      } catch (error) {
        console.error('发布请求错误详情：', error)

        // 更详细的错误处理
        if (error.response) {
          // 服务器返回了错误状态码
          const status = error.response.status
          const data = error.response.data

          console.error('错误响应:', error.response)

          switch (status) {
            case 400:
              this.$message.error('请求参数错误：' + (data.msg || '请检查填写的信息'))
              break
            case 401:
              this.$message.error('登录已过期，请重新登录')
              this.$router.push('/login')
              break
            case 403:
              this.$message.error('权限不足，无法发布物品')
              break
            case 413:
              this.$message.error('图片文件过大，请压缩后重新上传')
              break
            case 500:
              this.$message.error('服务器内部错误，请稍后重试')
              break
            default:
              this.$message.error('发布失败：' + (data.msg || '服务器错误'))
          }
        } else if (error.request) {
          // 请求发送但没有收到响应
          console.error('未收到响应:', error.request)
          this.$message.error('网络错误，请检查网络连接后重试')
        } else {
          // 其他错误
          this.$message.error('发布失败：' + error.message)
        }
      } finally {
        this.publishing = false
      }
    },

    // 取消发布
    handleCancel() {
      this.$confirm('确定要取消发布吗？已填写的内容将不会保存。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$router.back()
      })
    }
  }
}
</script>

<style scoped>
.item-publish-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.publish-form {
  margin-top: 20px;
}

.form-hint {
  margin-top: 10px;
  color: #999;
  font-size: 12px;
}

::v-deep .el-upload--picture-card {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

::v-deep .el-upload-list--picture-card .el-upload-list__item {
  width: 100px;
  height: 100px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .item-publish-container {
    padding: 10px;
  }

  ::v-deep .el-form-item__label {
    width: 80px !important;
  }

  ::v-deep .el-form-item__content {
    margin-left: 80px !important;
  }
}
</style>