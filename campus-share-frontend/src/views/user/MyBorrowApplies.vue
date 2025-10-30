<template>
  <div class="my-applies-container">
    <!-- 调试信息 -->
    <div class="debug-info">
      <p>数据长度：{{ appliesList.length }} 条</p>
      <p>当前筛选：{{ statusFilter || '全部' }}</p>
      <p>加载状态：{{ loading ? '加载中' : '已完成' }}</p>
      <p v-if="appliesList.length > 0">第一条数据：{{ JSON.stringify(appliesList[0]) }}</p>
    </div>

    <div v-if="appliesList.length > 0" style="background: #f0f9ff; padding: 10px; margin: 10px 0; border-radius: 4px;">
      <h4>测试显示 - 数据绑定确认：</h4>
      <div v-for="(item, index) in appliesList.slice(0, 2)" :key="item.id">
        {{ index + 1 }}. {{ item.itemName }} - {{ item.applyTime }}
      </div>
    </div>

    <el-card class="applies-card">
      <template #header>
        <div class="card-header">
          <span>我的借用申请</span>
        </div>
      </template>

      <!-- 状态筛选栏 -->
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" @change="handleFilterChange">
          <el-radio :label="''">全部状态</el-radio>
          <el-radio :label="0">待审核</el-radio>
          <el-radio :label="1">已通过</el-radio>
          <el-radio :label="2">已拒绝</el-radio>
        </el-radio-group>
        <el-button type="primary" @click="fetchApplies" :loading="loading">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>

      <!-- 申请列表表格 -->
      <!-- 申请列表表格 -->
      <el-table
          v-loading="loading"
          :data="appliesList"
          style="width: 100%; margin-top: 15px;"
          :row-key="getRowKey"
      >
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="itemName" label="物品名称" width="200">
          <template #default="scope">
      <span class="item-link" @click="goToItemDetail(scope.row.itemId)">
        {{ scope.row.itemName }}
      </span>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180"></el-table-column>
        <el-table-column prop="expectedBorrowTime" label="预计开始时间" width="180"></el-table-column>
        <el-table-column prop="expectedReturnTime" label="预计结束时间" width="180"></el-table-column>
        <el-table-column label="审核状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="showApplyDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && appliesList.length > 0" class="backup-table">
        <h4>备用表格显示（如果上面表格不显示）：</h4>
        <table style="width: 100%; border-collapse: collapse; border: 1px solid #ddd;">
          <thead>
          <tr style="background: #f5f7fa;">
            <th style="padding: 8px; border: 1px solid #ddd;">物品名称</th>
            <th style="padding: 8px; border: 1px solid #ddd;">申请时间</th>
            <th style="padding: 8px; border: 1px solid #ddd;">状态</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="item in appliesList" :key="item.id" style="border: 1px solid #ddd;">
            <td style="padding: 8px; border: 1px solid #ddd;">{{ item.itemName }}</td>
            <td style="padding: 8px; border: 1px solid #ddd;">{{ item.applyTime }}</td>
            <td style="padding: 8px; border: 1px solid #ddd;">{{ getStatusText(item.status) }}</td>
          </tr>
          </tbody>
        </table>
      </div>
      <!-- 无数据提示 -->
      <div v-if="!loading && appliesList.length === 0" class="no-data">
        <el-empty description="暂无借用申请记录"></el-empty>
      </div>
    </el-card>

    <!-- 申请详情弹窗 -->
    <el-dialog
        v-model="detailVisible"
        title="申请详情"
        width="60%"
        destroy-on-close
    >
      <el-descriptions v-if="currentApply" column="1" border>
        <el-descriptions-item label="申请ID">{{ currentApply.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="物品名称">{{ currentApply.itemName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentApply.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentApply.applyTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预计借用时间">
          {{ currentApply.expectedBorrowTime || '-' }} 至 {{ currentApply.expectedReturnTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getStatusType(currentApply.status)">
            {{ getStatusText(currentApply.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="拒绝原因" v-if="currentApply.rejectReason">
          {{ currentApply.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getMyBorrowApplies } from '@/api/borrowApply'

export default {
  name: 'MyBorrowApplies',
  data() {
    return {
      loading: false,
      appliesList: [],
      statusFilter: '',
      detailVisible: false,
      currentApply: null
    }
  },
  created() {
    this.fetchApplies()
  },
  methods: {
    /**
     * 获取申请列表数据 - 修复版本
     */
    async fetchApplies() {
      this.loading = true
      try {
        const params = {
          status: this.statusFilter || undefined,
          pageNum: 1,
          pageSize: 10
        }
        console.log('请求参数:', params)

        const res = await getMyBorrowApplies(params)
        console.log('接口返回数据:', res)

        if (res.code === 200) {
          // 直接使用后端返回的数据，只做必要的时间格式处理
          this.appliesList = (res.data.records || []).map(apply => {
            console.log('处理单条数据:', apply)
            return {
              ...apply,
              // 处理时间格式
              applyTime: this.formatTime(apply.applyTime),
              expectedBorrowTime: this.formatDate(apply.expectedBorrowTime),
              expectedReturnTime: this.formatDate(apply.expectedReturnTime)
            }
          })

          console.log('最终数据列表:', this.appliesList)
        } else {
          this.$message.error('获取数据失败: ' + (res.msg || '未知错误'))
          this.appliesList = []
        }
      } catch (error) {
        console.error('请求异常:', error)
        this.$message.error('网络错误，请重试')
        this.appliesList = []
      } finally {
        this.loading = false
      }
    },

    /**
     * 格式化日期时间（带T的格式）
     */
    formatTime(timeStr) {
      if (!timeStr) return '-'
      // 处理 "2025-10-29T20:22:56" 这种格式
      return timeStr.toString().replace('T', ' ')
    },

    /**
     * 格式化日期（不带时间的格式）
     */
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return dateStr.toString()
    },

    /**
     * 获取行唯一key
     */
    getRowKey(row) {
      return row.id
    },

    /**
     * 筛选条件变更时触发
     */
    handleFilterChange() {
      this.fetchApplies()
    },

    /**
     * 根据状态码获取状态文本
     */
    getStatusText(status) {
      const statusMap = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝'
      }
      return statusMap[status] || '未知状态'
    },

    /**
     * 根据状态码获取标签类型
     */
    getStatusType(status) {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return typeMap[status] || 'default'
    },

    /**
     * 跳转到物品详情页
     */
    goToItemDetail(itemId) {
      if (itemId) {
        this.$router.push(`/items/${itemId}`)
      }
    },

    /**
     * 显示申请详情
     */
    showApplyDetail(apply) {
      this.currentApply = { ...apply }
      this.detailVisible = true
    }
  }
}
</script>

<style scoped>
.my-applies-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.applies-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.item-link {
  color: #409eff;
  cursor: pointer;
  font-weight: 500;
}

.item-link:hover {
  text-decoration: underline;
}

.no-data {
  margin: 50px 0;
  text-align: center;
}

.debug-info {
  padding: 12px;
  background: #f0f9ff;
  border: 1px solid #bee5eb;
  border-radius: 6px;
  margin-bottom: 15px;
  font-size: 14px;
  color: #31708f;
}

.debug-info p {
  margin: 4px 0;
}

/* 完全重写表格样式 - 简化版本 */
::v-deep .el-table {
  font-size: 14px;
}

::v-deep .el-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

::v-deep .el-table td {
  color: #303133;
}

/* 确保表格行可见 */
::v-deep .el-table__body-wrapper {
  overflow-x: auto;
}

::v-deep .el-table__row {
  height: auto;
  min-height: 48px;
}

::v-deep .el-table__cell {
  padding: 8px 0;
  word-break: break-all;
}
</style>