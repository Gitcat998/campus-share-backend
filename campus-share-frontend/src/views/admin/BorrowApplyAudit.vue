<template>
  <div class="borrow-apply-audit">
    <!-- 调试信息 -->
    <div class="debug-info">
      <p><strong>申请列表状态：</strong>数据长度：{{ applyList.length }} 条，总记录数：{{ total }}</p>
      <p><strong>加载状态：</strong>{{ loading ? '加载中' : '已完成' }}</p>
      <p><strong>选中的申请：</strong>{{ selectedApplyIds.join(', ') || '无' }}</p>
    </div>

    <el-card class="apply-audit-card">
      <template #header>
        <div class="card-header">
          <span>借用申请审核管理</span>
          <div>
            <el-button @click="diagnoseAPI" type="warning" size="mini">API诊断</el-button>
            <el-button @click="forceRerender" type="info" size="mini">强制刷新表格</el-button>
          </div>
        </div>
      </template>

      <!-- 筛选区 -->
      <div class="filter-bar">
        <el-select
            v-model="status"
            placeholder="请选择申请状态"
            clearable
            style="width: 200px; margin-right: 15px;"
            @change="handleFilterChange"
        >
          <el-option label="全部状态" value=""></el-option>
          <el-option label="待审核" value="0"></el-option>
          <el-option label="已同意" value="1"></el-option>
          <el-option label="已拒绝" value="2"></el-option>
        </el-select>

        <el-button
            type="primary"
            icon="el-icon-refresh"
            @click="resetFilter"
            style="margin-left: 15px;"
        >
          重置
        </el-button>
      </div>

      <!-- 申请列表表格（仅保留简单表格） -->
      <div class="apply-table" v-loading="loading">
        <div class="simple-table-container">
          <div v-if="applyList.length === 0 && !loading" class="no-data">
            <el-empty description="暂无借用申请"></el-empty>
          </div>

          <div v-else class="simple-table">
            <div class="table-header">
              <div class="table-row header-row">
                <div class="col-select">
                  <el-checkbox
                      v-model="selectAll"
                      @change="handleSelectAll"
                      :indeterminate="selectedApplyIds.length > 0 && selectedApplyIds.length < applyList.length"
                  ></el-checkbox>
                </div>
                <div class="col-id">申请ID</div>
                <div class="col-name">物品名称</div>
                <div class="col-user">申请人</div>
                <div class="col-borrow-time">期望借用时间</div>
                <div class="col-return-time">期望归还时间</div>
                <div class="col-status">状态</div>
                <div class="col-actions">操作</div>
              </div>
            </div>

            <div class="table-body">
              <div
                  v-for="item in applyList"
                  :key="item.id"
                  class="table-row data-row"
              >
                <div class="col-select">
                  <el-checkbox
                      v-model="selectedApplyIds"
                      :label="item.id"
                      @change="handleSingleSelect"
                  ></el-checkbox>
                </div>
                <div class="col-id">{{ item.id }}</div>
                <div class="col-name" :title="item.itemName">{{ item.itemName }}</div>
                <div class="col-user">{{ item.username}}</div>
                <div class="col-borrow-time">{{ item.expectedBorrowTime }}</div>
                <div class="col-return-time">{{ item.expectedReturnTime }}</div>
                <div class="col-status">
                  <span class="status-tag" :class="getStatusClass(item.status)">
                    {{ getStatusText(item.status) }}
                  </span>
                </div>
                <div class="col-actions">
                  <template v-if="item.status === 0">
                    <button
                        class="btn btn-success"
                        @click="handleAgree(item.id)"
                        title="同意申请"
                    >
                      同意
                    </button>
                    <button
                        class="btn btn-danger"
                        @click="handleReject(item.id)"
                        title="拒绝申请"
                    >
                      拒绝
                    </button>
                  </template>
                  <template v-else>
                    <span class="operated">已处理</span>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 批量操作与分页 -->
      <div class="batch-operation">
        <div class="batch-btn-group">
          <el-button
              type="success"
              @click="batchAgree"
              :disabled="selectedApplyIds.length === 0"
              icon="el-icon-check"
          >
            批量同意 ({{ selectedApplyIds.length }})
          </el-button>
          <el-button
              type="danger"
              @click="batchReject"
              :disabled="selectedApplyIds.length === 0"
              icon="el-icon-close"
              style="margin-left: 10px;"
          >
            批量拒绝 ({{ selectedApplyIds.length }})
          </el-button>
        </div>

        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[10, 20, 50]"
            :page-size="pageSize"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
        ></el-pagination>
      </div>

      <!-- 拒绝原因弹窗 -->
      <el-dialog
          title="拒绝申请"
          :visible.sync="showRejectDialog"
          width="400px"
          :before-close="handleCloseRejectDialog"
      >
        <el-form :model="rejectForm" ref="rejectForm" :rules="rejectRules">
          <el-form-item label="拒绝原因" prop="reason">
            <el-input
                type="textarea"
                v-model="rejectForm.reason"
                rows="4"
                placeholder="请输入拒绝原因"
            ></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button @click="showRejectDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确定</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { queryBorrowApplies, auditBorrowApply } from '@/api/borrowApply'
import { Message } from 'element-ui'

export default {
  name: 'BorrowApplyAudit',
  data() {
    return {
      // 申请列表数据
      applyList: [],
      loading: false,
      total: 0,
      pageNum: 1,
      pageSize: 10,

      // 筛选条件
      status: '', // 申请状态：0-待审核，1-已同意，2-已拒绝

      // 批量选择的申请ID
      selectedApplyIds: [],
      selectAll: false, // 全选状态

      // 拒绝弹窗相关
      showRejectDialog: false,
      currentApplyId: null, // 当前处理的申请ID
      rejectForm: {
        reason: ''
      },
      rejectRules: {
        reason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
      }
    }
  },
  created() {
    console.log('=== 借用申请审核页面初始化 ===')
    this.fetchApplyList()
  },
  methods: {
    // 强制重新渲染表格
    forceRerender() {
      console.log('强制重新渲染表格')
      this.$forceUpdate()
    },

    // 获取借用申请列表
    async fetchApplyList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: this.status || undefined
        }
        console.log('请求借用申请列表参数:', params)

        const res = await queryBorrowApplies(params)
        console.log('借用申请列表响应:', res)

        if (res.code === 200) {
          this.applyList = res.data.records || []
          this.total = res.data.total || 0

          // 检查数据完整性
          console.log('申请列表数据详情:')
          this.applyList.forEach((item, index) => {
            console.log(`申请 ${index}: id=${item.id}, itemName=${item.itemName}, status=${item.status}`)
            if (!item.id) {
              console.warn('发现空ID的申请:', item)
            }
          })

          // 重置选择状态
          this.selectedApplyIds = []
          this.selectAll = false
        } else {
          Message.error('获取申请列表失败：' + (res.msg || '未知错误'))
          this.applyList = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取申请列表失败:', error)
        Message.error('网络错误：' + error.message)
        this.applyList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    // 简单表格的状态样式
    getStatusClass(status) {
      const classMap = {
        0: 'status-pending',
        1: 'status-approved',
        2: 'status-rejected'
      }
      return classMap[status] || 'status-default'
    },

    // 简单表格的状态文本
    getStatusText(status) {
      const textMap = {
        0: '待审核',
        1: '已同意',
        2: '已拒绝'
      }
      return textMap[status] || '未知状态'
    },

    // 筛选条件变化
    handleFilterChange() {
      this.pageNum = 1
      this.fetchApplyList()
    },

    // 重置筛选条件
    resetFilter() {
      this.status = ''
      this.pageNum = 1
      this.fetchApplyList()
    },

    // 分页处理
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.pageNum = 1
      this.fetchApplyList()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.fetchApplyList()
    },

    // 全选处理
    handleSelectAll(checked) {
      if (checked) {
        this.selectedApplyIds = this.applyList.map(item => item.id)
      } else {
        this.selectedApplyIds = []
      }
      console.log('全选状态:', checked, '选中的申请ID:', this.selectedApplyIds)
    },

    // 单个选择处理
    handleSingleSelect() {
      // 当所有行都被选中时，勾选全选框
      if (this.selectedApplyIds.length === this.applyList.length) {
        this.selectAll = true
      } else {
        this.selectAll = false
      }
      console.log('单个选择变化，选中的申请ID:', this.selectedApplyIds)
    },

    // 同意单个申请
    async handleAgree(applyId) {
      console.log('=== 同意申请开始 ===')
      console.log('applyId:', applyId, '类型:', typeof applyId)

      // 添加参数验证
      if (!applyId) {
        console.error('applyId为空！')
        Message.error('申请ID不能为空')
        return
      }

      this.$confirm('确定要同意该借用申请吗？', '操作确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }).then(async () => {
        try {
          console.log('调用审核API，applyId:', applyId)
          const res = await auditBorrowApply(applyId, 1, '')
          console.log('审核API响应:', res)

          if (res.code === 200) {
            Message.success('同意申请成功')
            this.fetchApplyList() // 刷新列表
          } else {
            Message.error('操作失败：' + (res.msg || '未知错误'))
          }
        } catch (error) {
          console.error('同意申请失败:', error)
          Message.error('操作失败：' + error.message)
        }
      }).catch(() => {
        console.log('用户取消操作')
      })
    },

    // 拒绝单个申请（打开弹窗）
    handleReject(applyId) {
      console.log('拒绝申请，applyId:', applyId)

      if (!applyId) {
        console.error('applyId为空！')
        Message.error('申请ID不能为空')
        return
      }

      this.currentApplyId = applyId
      this.rejectForm.reason = ''
      this.showRejectDialog = true
    },

    // 关闭拒绝弹窗
    handleCloseRejectDialog() {
      this.showRejectDialog = false
      this.currentApplyId = null
    },

    // 确认拒绝
    async confirmReject() {
      this.$refs.rejectForm.validate(async (valid) => {
        if (valid && this.currentApplyId) {
          try {
            console.log('确认拒绝，applyId:', this.currentApplyId, '原因:', this.rejectForm.reason)
            const res = await auditBorrowApply(this.currentApplyId, 2, this.rejectForm.reason)
            console.log('拒绝API响应:', res)

            if (res.code === 200) {
              Message.success('拒绝申请成功')
              this.showRejectDialog = false
              this.fetchApplyList()
            } else {
              Message.error('操作失败：' + (res.msg || '未知错误'))
            }
          } catch (error) {
            console.error('拒绝申请失败:', error)
            Message.error('操作失败：' + error.message)
          }
        }
      })
    },

    // 批量同意
    batchAgree() {
      if (this.selectedApplyIds.length === 0) {
        Message.warning('请选择需要同意的申请')
        return
      }

      console.log('批量同意，选中的申请ID:', this.selectedApplyIds)

      this.$confirm(`确定要同意选中的 ${this.selectedApplyIds.length} 条申请吗？`, '批量操作', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }).then(() => {
        this.batchOperate(1)
      })
    },

    // 批量拒绝
    batchReject() {
      if (this.selectedApplyIds.length === 0) {
        Message.warning('请选择需要拒绝的申请')
        return
      }

      console.log('批量拒绝，选中的申请ID:', this.selectedApplyIds)

      this.rejectForm.reason = ''
      this.showRejectDialog = true
      this.currentApplyId = 'batch' // 标记为批量操作
    },

    // 批量操作执行
    async batchOperate(status) {
      this.loading = true
      let successCount = 0
      let failCount = 0
      let errorMessages = []

      try {
        for (const applyId of this.selectedApplyIds) {
          try {
            console.log(`批量处理申请 ${applyId}, 状态: ${status}`)

            if (!applyId) {
              console.warn('跳过空的applyId')
              failCount++
              continue
            }

            const res = await auditBorrowApply(
                applyId,
                status,
                status === 2 ? this.rejectForm.reason : ''
            )

            if (res.code === 200) {
              successCount++
            } else {
              failCount++
              errorMessages.push(`申请 ${applyId}: ${res.msg || '未知错误'}`)
            }
          } catch (error) {
            failCount++
            errorMessages.push(`申请 ${applyId}: ${error.message}`)
            console.error(`处理申请 ${applyId} 失败:`, error)
          }
        }

        if (failCount === 0) {
          Message.success(`批量操作完成：成功 ${successCount} 条`)
        } else {
          Message.warning(`批量操作完成：成功 ${successCount} 条，失败 ${failCount} 条`)
          console.error('失败详情:', errorMessages)
        }

        this.selectedApplyIds = []
        this.selectAll = false
        this.fetchApplyList()
      } catch (error) {
        console.error('批量操作失败:', error)
        Message.error('批量操作失败：' + error.message)
      } finally {
        this.loading = false
        if (status === 2) {
          this.showRejectDialog = false
        }
      }
    },

    // API诊断
    async diagnoseAPI() {
      console.log('=== 借用申请API诊断 ===')
      try {
        const res = await queryBorrowApplies({pageNum: 1, pageSize: 5})
        console.log('API原始响应:', JSON.stringify(res, null, 2))

        if (res.code === 200 && res.data.records) {
          console.log('第一条申请数据:', res.data.records[0])
        }

        Message.success('API诊断完成，请查看控制台')
      } catch (error) {
        console.error('API诊断失败:', error)
        Message.error('API调用失败：' + error.message)
      }
    }
  }
}
</script>

<style scoped>
.borrow-apply-audit {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.apply-audit-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.apply-table {
  margin: 15px 0;
}

.no-data {
  margin: 40px 0;
  text-align: center;
}

.batch-operation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding: 10px 0;
}

.batch-btn-group {
  display: flex;
}

.operated {
  color: #909399;
  font-size: 14px;
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

/* 简单表格样式 */
.simple-table-container {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.simple-table {
  width: 100%;
}

.table-header {
  background-color: #f5f7fa;
}

.table-row {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
}

.table-row:last-child {
  border-bottom: none;
}

.header-row {
  font-weight: 600;
  color: #606266;
  background-color: #f5f7fa;
}

.data-row {
  background-color: white;
  transition: background-color 0.3s;
}

.data-row:hover {
  background-color: #f5f7fa;
}

/* 列宽定义 */
.col-select {
  width: 50px;
  padding: 12px 8px;
  text-align: center;
}
.col-id {
  width: 100px;
  padding: 12px 8px;
  text-align: center;
}
.col-name {
  width: 200px;
  padding: 12px 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.col-user {
  width: 120px;
  padding: 12px 8px;
}
.col-borrow-time {
  width: 160px;
  padding: 12px 8px;
}
.col-return-time {
  width: 160px;
  padding: 12px 8px;
}
.col-status {
  width: 120px;
  padding: 12px 8px;
}
.col-actions {
  width: 200px;
  padding: 8px;
  text-align: center;
}

/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-approved {
  background: #f0f9eb;
  color: #67c23a;
}

.status-rejected {
  background: #fef0f0;
  color: #f56c6c;
}

.status-default {
  background: #f4f4f5;
  color: #909399;
}

/* 按钮样式 */
.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin: 0 2px;
  transition: all 0.3s;
}

.btn-success {
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #c2e7b0;
}

.btn-success:hover {
  background: #67c23a;
  color: white;
}

.btn-danger {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.btn-danger:hover {
  background: #f56c6c;
  color: white;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .batch-operation {
    flex-direction: column;
    gap: 15px;
  }

  .batch-btn-group {
    justify-content: center;
    flex-wrap: wrap;
  }

  .simple-table {
    overflow-x: auto;
  }

  .table-row {
    min-width: 800px;
  }
}
</style>