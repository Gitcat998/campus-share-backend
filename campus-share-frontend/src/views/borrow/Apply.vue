<template>
  <div class="borrow-apply-container">
    <el-card header="申请借用" v-loading="loading">
      <!-- 新增：数据未加载时显示提示，避免空白 -->
      <div v-if="Object.keys(item).length === 0 && !loading" class="empty-tip">
        物品信息加载失败，请返回重试
      </div>

      <!-- 数据加载完成后再渲染表单 -->
      <el-form
          ref="applyForm"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="apply-form"
          v-if="Object.keys(item).length > 0"
      >
        <!-- 物品名称回显 -->
        <el-form-item label="物品名称">
          <el-input
              v-model="item.name"
              disabled
          ></el-input>
        </el-form-item>

        <!-- 可借时长回显（修正suffix位置） -->
        <el-form-item label="可借时长">
          <el-input
              v-model="item.borrowDuration"
              suffix="天"
          disabled
          ></el-input>
        </el-form-item>

        <!-- 借用开始时间 -->
        <el-form-item label="借用开始时间" prop="startTime">
          <el-date-picker
              v-model="form.startTime"
              type="datetime"
              placeholder="选择开始时间"
              :min-date="new Date()"
              style="width: 100%"
          ></el-date-picker>
        </el-form-item>

        <!-- 借用结束时间 -->
        <el-form-item label="借用结束时间" prop="endTime">
          <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="选择结束时间"
              :min-date="form.startTime"
              style="width: 100%"
          ></el-date-picker>
          <div class="form-hint">
            提示：最长可借 {{ item.borrowDuration || 0 }} 天
          </div>
        </el-form-item>

        <!-- 借用理由 -->
        <el-form-item label="借用理由" prop="reason">
          <el-input
              v-model="form.reason"
              type="textarea"
              rows="3"
              placeholder="请简要说明借用理由（选填）"
              maxlength="200"
          ></el-input>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              @click="handleSubmit"
              :loading="submitting"
          >
            提交申请
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getItemDetail } from '../../api/item'
import { submitBorrowApply } from '../../api/borrowApply'
import dayjs from 'dayjs'

export default {
  name: 'Apply',
  data() {
    const itemId = this.$route.query.itemId // 从路由查询参数获取物品ID
    return {
      itemId,
      item: {}, // 存储物品详情（回显用）
      loading: false, // 页面加载状态
      submitting: false, // 提交按钮加载状态（防重复提交）
      form: {
        startTime: dayjs().toDate(), // 默认开始时间为当前时间
        endTime: null, // 结束时间（选择后赋值）
        reason: '', // 借用理由
        itemId: itemId // 关联物品ID（提交给后端）
      },
      rules: {
        startTime: [
          { required: true, message: '请选择借用开始时间', trigger: 'change' }
        ],
        endTime: [
          { required: true, message: '请选择借用结束时间', trigger: 'change' },
          { validator: this.checkMaxDays, trigger: 'change' } // 校验最长可借时长
        ]
      }
    }
  },
  mounted() {

    console.log('Apply.vue 获取的 itemId:', this.$route.params.itemId)

    // 打印路由参数，确认itemId是否正确获取
    console.log('当前路由查询参数：', this.$route.query)
    console.log('获取的物品ID：', this.itemId)
    this.itemId = this.$route.params.itemId; // 从 params 获取
    // 校验itemId有效性（非空且为数字）
    if (!this.itemId || isNaN(Number(this.itemId))) {
      this.$message.error('缺少有效物品ID，无法申请')
      // 1.5秒后自动返回上一页
      setTimeout(() => {
        this.$router.back()
      }, 1500)
      return
    }

    // 加载物品信息
    this.loadItemInfo()
  },
  methods: {
    // 加载物品详情（用于回显名称、可借时长）
    async loadItemInfo() {
      this.loading = true
      try {
        // 调试：打印接口调用的完整 URL
        console.log('调用接口：', `http://localhost:8080/api/v1/items/${this.itemId}`)
        // 调用接口获取物品数据（确保api中配置了正确的后端端口）
        const res = await getItemDetail(this.itemId)
        // 调试：打印接口响应
        console.log('接口响应：', res)

        // 校验接口响应（必须有code=200和data数据）
        if (res.code !== 200 || !res.data) {
          throw new Error(res.msg || '物品信息加载失败')
        }

        // 赋值物品数据（关键：确保res.data中有name和borrowDuration字段）
        this.item = res.data
        console.log('成功加载物品信息：', this.item)

        // 设置默认结束时间（开始时间 + 可借时长）
        const maxDays = this.item.borrowDuration || 1 // 默认为1天
        this.form.endTime = dayjs(this.form.startTime)
            .add(maxDays, 'day')
            .toDate()

      } catch (error) {
        // 打印错误详情，方便排查
        console.error('物品信息加载失败：', error)
        this.$message.error('页面加载失败：' + error.message)
        // 失败后返回上一页
        setTimeout(() => {
          this.$router.back()
        }, 1500)
      } finally {
        this.loading = false // 无论成功失败，都关闭加载状态
      }
    },

    // 校验结束时间是否超过最大可借时长
    checkMaxDays(rule, value, callback) {
      // 未选择时间时不校验
      if (!value || !this.form.startTime) {
        return callback()
      }

      const start = dayjs(this.form.startTime)
      const end = dayjs(value)

      // 1. 校验结束时间是否晚于开始时间
      if (end.isBefore(start)) {
        return callback(new Error('结束时间不能早于开始时间'))
      }

      // 2. 校验是否超过最大可借时长
      const maxDays = this.item.borrowDuration || 0
      if (maxDays > 0) {
        const days = end.diff(start, 'day', true) // 支持小数天数（如1.5天）
        if (days > maxDays) {
          return callback(new Error(`借用时长不能超过 ${maxDays} 天`))
        }
      }

      // 校验通过
      callback()
    },

    // 提交借用申请
    handleSubmit() {
      this.$refs.applyForm.validate(async (valid) => {
        // 表单校验通过且未在提交中，才执行提交
        if (valid && !this.submitting) {
          this.submitting = true // 锁定提交按钮，防重复点击
          try {
            // 格式化日期为后端需要的格式（YYYY-MM-DD HH:mm:ss）
            const submitData = {
              itemId: this.itemId, // 确保 itemId 正确传递
              expectedBorrowTime: dayjs(this.form.startTime).format('YYYY-MM-DD'), // 仅日期部分
              expectedReturnTime: dayjs(this.form.endTime).format('YYYY-MM-DD'), // 仅日期部分
              reason: this.form.reason // 借用理由（可选）
            }

            // 提交申请到后端
            await submitBorrowApply(submitData)
            this.$message.success('申请提交成功，等待审核！')

            console.log('即将跳转至我的申请列表') // 新增日志
            // 跳转到我的申请列表页
            this.$router.push('/borrow-applies/my')

          } catch (error) {
            console.error('申请提交失败：', error)
            this.$message.error('提交失败：' + (error.message || '请稍后重试'))
          } finally {
            this.submitting = false // 解锁提交按钮
          }
        }
      })
    },

    // 取消申请，返回上一页
    handleCancel() {
      this.$router.back()
    }
  }
}
</script>

<style scoped>
.borrow-apply-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.apply-form {
  margin-top: 20px;
}

.form-hint {
  margin-top: 10px;
  color: #666;
  font-size: 12px;
}

.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
}

/* 优化禁用输入框样式 */
::v-deep .el-input.is-disabled .el-input__inner {
  background-color: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
}
</style>