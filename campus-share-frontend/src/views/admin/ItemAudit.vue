<template>
  <div class="admin-item-audit">
    <!-- 调试信息 -->
    <div class="debug-info">
      <p><strong>数据状态：</strong>数据长度：{{ auditList.length }} 条，总记录数：{{ total }}</p>
      <p><strong>加载状态：</strong>{{ loading ? '加载中' : '已完成' }}</p>
      <p><strong>分页：</strong>当前页码：{{ pageNum }}，每页：{{ pageSize }}</p>
    </div>

    <el-card class="item-audit-card">
      <template #header>
        <div class="card-header">
          <span>物品审核管理</span>
          <div>
            <el-button @click="diagnoseAPI" type="warning" size="mini">API诊断</el-button>
            <el-button @click="forceRerender" type="info" size="mini">强制刷新</el-button>
            <el-button @click="emergencyFix" type="danger" size="mini">紧急修复</el-button>
          </div>
        </div>
      </template>

      <!-- 筛选与搜索区 -->
      <div class="filter-bar">
        <el-select
            v-model="itemTypeId"
            placeholder="请选择物品分类"
            clearable
            style="width: 200px; margin-right: 15px;"
            @change="handleFilterChange"
        >
          <el-option
              v-for="type in itemTypes"
              :key="type.id"
              :label="type.typeName"
              :value="type.id"
          ></el-option>
        </el-select>

        <el-input
            v-model="searchName"
            placeholder="输入物品名称搜索"
            clearable
            style="width: 300px;"
            @keyup.enter="handleFilterChange"
        >
          <template #append>
            <el-button
                icon="el-icon-search"
                @click="handleFilterChange"
            ></el-button>
          </template>
        </el-input>

        <el-button
            type="primary"
            icon="el-icon-refresh"
            @click="resetFilter"
            style="margin-left: 15px;"
        >
          重置
        </el-button>
      </div>

      <!-- 简单表格 -->
      <div class="simple-table-container" v-loading="loading">
        <div v-if="auditList.length === 0 && !loading" class="no-data">
          <el-empty description="暂无待审核物品"></el-empty>
        </div>

        <div v-else class="simple-table">
          <div class="table-header">
            <div class="table-row header-row">
              <div class="col-checkbox">
                <input
                    type="checkbox"
                    :checked="isAllSelected"
                    @change="toggleSelectAll"
                >
              </div>
              <div class="col-image">图片</div>
              <div class="col-id">ID</div>
              <div class="col-name">物品名称</div>
              <div class="col-type">分类</div>
              <div class="col-publisher">发布者</div>
              <div class="col-status">状态</div>
              <div class="col-actions">操作</div>
            </div>
          </div>

          <div class="table-body">
            <div
                v-for="item in auditList"
                :key="item.id"
                class="table-row data-row"
                :class="{ selected: selectedItems.includes(item.id) }"
            >
              <div class="col-checkbox">
                <input
                    type="checkbox"
                    :checked="selectedItems.includes(item.id)"
                    @change="toggleSelectItem(item.id)"
                >
              </div>
              <div class="col-image">
                <div class="item-image" @click="previewImage(item.mainImageUrl)">
                  <img
                      v-if="item.mainImageUrl"
                      :src="item.mainImageUrl"
                      :alt="item.name"
                      @error="handleImageError"
                  >
                  <div v-else class="no-image">无图片</div>
                </div>
              </div>
              <div class="col-id">{{ item.id }}</div>
              <div class="col-name" :title="item.name">{{ item.name }}</div>
              <div class="col-type">{{ item.typeName }}</div>
              <div class="col-publisher">{{ item.publisherName || '未知' }}</div>
              <div class="col-status">
                <span class="status-tag" :class="getStatusClass(item.status)">
                  {{ item.statusText }}
                </span>
              </div>
              <div class="col-actions">
                <button
                    class="btn btn-success"
                    @click="handleAudit(item, 1)"
                    title="通过审核"
                >
                  ✓ 通过
                </button>
                <button
                    class="btn btn-danger"
                    @click="handleAudit(item, 3)"
                    title="拒绝审核"
                >
                  ✗ 拒绝
                </button>
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
              @click="batchAudit(1)"
              :disabled="selectedItems.length === 0"
              icon="el-icon-check"
          >
            批量通过 ({{ selectedItems.length }})
          </el-button>
          <el-button
              type="danger"
              @click="batchAudit(3)"
              :disabled="selectedItems.length === 0"
              icon="el-icon-close"
              style="margin-left: 10px;"
          >
            批量拒绝 ({{ selectedItems.length }})
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

      <!-- 图片预览模态框 -->
      <div v-if="showImagePreview" class="image-preview-modal" @click="closeImagePreview">
        <div class="preview-content">
          <img :src="previewImageUrl" alt="预览图片">
          <button class="close-btn" @click="closeImagePreview">×</button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getPendingItems, auditItem } from '@/api/admin'
import { getItemTypes } from '@/api/item'
import { Message } from 'element-ui'

export default {
  name: 'ItemAudit',
  data() {
    return {
      // 列表数据
      auditList: [],
      loading: false,
      total: 0,
      pageNum: 1,
      pageSize: 10,

      // 筛选条件
      itemTypeId: '',
      searchName: '',
      itemTypes: [],

      // 批量选择
      selectedItems: [],

      // 图片预览
      showImagePreview: false,
      previewImageUrl: ''
    }
  },
  computed: {
    isAllSelected() {
      return this.auditList.length > 0 && this.selectedItems.length === this.auditList.length
    }
  },
  created() {
    console.log('=== 页面初始化 ===')
    this.loadItemTypes()
    this.fetchPendingItems()
  },
  methods: {
    // 紧急修复：重置所有状态
    emergencyFix() {
      console.log('执行紧急修复')
      this.auditList = []
      this.loading = false
      this.pageNum = 1
      this.selectedItems = []

      // 重新获取数据
      setTimeout(() => {
        this.fetchPendingItems()
      }, 100)
    },

    // 强制重新渲染
    forceRerender() {
      console.log('强制重新渲染')
      this.fetchPendingItems()
    },

    // 全选/取消全选
    toggleSelectAll() {
      if (this.isAllSelected) {
        this.selectedItems = []
      } else {
        this.selectedItems = this.auditList.map(item => item.id)
      }
    },

    // 选择单个项目
    toggleSelectItem(itemId) {
      const index = this.selectedItems.indexOf(itemId)
      if (index > -1) {
        this.selectedItems.splice(index, 1)
      } else {
        this.selectedItems.push(itemId)
      }
    },

    // 根据状态码获取CSS类
    getStatusClass(status) {
      const classMap = {
        0: 'status-pending', // 待审核
        1: 'status-approved', // 通过
        3: 'status-rejected'  // 拒绝
      }
      return classMap[status] || 'status-default'
    },

    // 图片预览
    previewImage(imageUrl) {
      if (imageUrl) {
        this.previewImageUrl = imageUrl
        this.showImagePreview = true
      }
    },

    // 关闭图片预览
    closeImagePreview() {
      this.showImagePreview = false
      this.previewImageUrl = ''
    },

    // 图片加载错误处理
    handleImageError(event) {
      event.target.style.display = 'none'
      event.target.parentElement.querySelector('.no-image').style.display = 'block'
    },

    // API诊断方法
    async diagnoseAPI() {
      console.log('=== 开始API诊断 ===')

      try {
        // 测试1: 直接调用API查看原始响应
        console.log('测试1: 获取待审核物品')
        const itemsRes = await getPendingItems({
          current: 1,
          size: 5
        })
        console.log('待审核物品原始响应:', JSON.stringify(itemsRes, null, 2))

        // 检查状态码为200
        if (itemsRes.code === 200) {
          let data = itemsRes.data
          // 处理嵌套数据
          if (data && data.data) {
            data = data.data
          }
          console.log('实际数据:', data.records || data)
        } else {
          console.error('接口返回错误:', itemsRes)
        }

        // 测试2: 测试分类API
        console.log('测试2: 获取分类')
        const typesRes = await getItemTypes()
        console.log('分类响应:', typesRes)

        Message.success('API诊断完成，请查看控制台')
      } catch (error) {
        console.error('诊断失败:', error)
        Message.error('诊断失败: ' + error.message)
      }
    },

    // 加载物品分类
    async loadItemTypes() {
      try {
        console.log('正在加载物品分类...')
        const res = await getItemTypes()
        console.log('分类接口返回:', res)

        // 检查状态码为200
        if (res.code === 200) {
          this.itemTypes = this.flattenItemTypes(res.data || [])
          console.log('加载分类成功，数量:', this.itemTypes.length)
        } else {
          console.error('分类接口返回错误:', res)
          Message.error('加载分类失败：' + (res.msg || '未知错误'))
        }
      } catch (error) {
        console.error('加载分类失败：', error)
        Message.error('加载分类失败：' + error.message)
      }
    },

    // 展平树形分类结构
    flattenItemTypes(treeData) {
      const result = []
      const flatten = (nodes) => {
        nodes.forEach(node => {
          result.push({
            id: node.id,
            typeName: node.typeName
          })
          if (node.children && node.children.length > 0) {
            flatten(node.children)
          }
        })
      }
      flatten(treeData)
      return result
    },

    // 获取待审核列表
    async fetchPendingItems() {
      console.log('=== 开始获取待审核列表 ===')
      this.loading = true

      try {
        // 使用正确的分页参数名
        const params = {
          current: this.pageNum,
          size: this.pageSize,
          itemTypeId: this.itemTypeId || undefined,
          name: this.searchName || undefined
        }
        console.log('请求参数:', params)

        const res = await getPendingItems(params)
        console.log('完整API响应:', res)

        // 检查状态码为200
        if (res.code === 200) {
          // 处理可能的多层嵌套数据
          let responseData = res.data

          // 如果 data 里面还有 data，则取内层的 data
          if (responseData && responseData.data) {
            responseData = responseData.data
          }

          this.auditList = responseData.records || responseData.list || []
          this.total = responseData.total || 0

          console.log('处理后的审核列表:', this.auditList)
          console.log('总记录数:', this.total)

          // 如果数据为空，显示提示信息
          if (this.auditList.length === 0) {
            console.log('数据为空，显示无数据状态')
          }
        } else {
          console.error('接口返回错误码:', res.code, '消息:', res.msg)
          Message.error('获取列表失败：' + (res.msg || '未知错误'))
          this.auditList = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取失败：', error)
        Message.error('网络错误：' + error.message)
        this.auditList = []
        this.total = 0
      } finally {
        this.loading = false
        console.log('数据加载完成，数据长度:', this.auditList.length)
      }
    },

    // 筛选条件变化
    handleFilterChange() {
      this.pageNum = 1
      this.selectedItems = []
      this.fetchPendingItems()
    },

    // 重置筛选条件
    resetFilter() {
      this.itemTypeId = ''
      this.searchName = ''
      this.pageNum = 1
      this.selectedItems = []
      this.fetchPendingItems()
    },

    // 分页相关
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.pageNum = 1
      this.selectedItems = []
      this.fetchPendingItems()
    },

    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.selectedItems = []
      this.fetchPendingItems()
    },

    // 单个审核操作
    handleAudit(item, status) {
      const actionText = status === 1 ? '通过' : '拒绝'
      this.$confirm(`确定要${actionText}物品"${item.name}"的审核吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.confirmAudit(item.id, status)
      })
    },

    // 执行审核
    async confirmAudit(itemId, status) {
      try {
        console.log('审核参数:', {itemId, status})

        const res = await auditItem(itemId, status)
        console.log('审核结果:', res)

        // 检查状态码为200
        if (res.code === 200) {
          Message.success(status === 1 ? '审核通过' : '拒绝成功')
          this.fetchPendingItems()
        } else {
          Message.error((status === 1 ? '审核失败' : '拒绝失败') + ': ' + (res.msg || '未知错误'))
        }
      } catch (error) {
        console.error('审核操作失败:', error)
        Message.error(status === 1 ? '审核失败' : '拒绝失败')
      }
    },

    // 批量审核
    batchAudit(status) {
      if (this.selectedItems.length === 0) {
        Message.warning('请选择需要操作的物品')
        return
      }

      const actionText = status === 1 ? '通过' : '拒绝'
      const selectedNames = this.auditList
          .filter(item => this.selectedItems.includes(item.id))
          .map(item => item.name)
          .join('、')

      this.$confirm(`确定要${actionText}选中的 ${this.selectedItems.length} 个物品吗？\n${selectedNames}`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.batchConfirmAudit(status)
      })
    },

    // 批量审核
    async batchConfirmAudit(status) {
      this.loading = true
      try {
        let successCount = 0
        let failCount = 0

        for (const id of this.selectedItems) {
          try {
            const res = await auditItem(id, status)
            // 检查状态码为200
            if (res.code === 200) {
              successCount++
            } else {
              failCount++
            }
          } catch (error) {
            failCount++
          }
        }

        if (failCount === 0) {
          Message.success(`批量${status === 1 ? '通过' : '拒绝'}成功，共处理 ${successCount} 个物品`)
        } else {
          Message.warning(`处理完成：成功 ${successCount} 个，失败 ${failCount} 个`)
        }

        this.selectedItems = []
        this.fetchPendingItems()
      } catch (error) {
        console.error('批量操作失败:', error)
        Message.error('批量操作失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.admin-item-audit {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.item-audit-card {
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

.no-data {
  margin: 40px 0;
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

/* 简单表格样式 */
.simple-table-container {
  margin-top: 15px;
}

.simple-table {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
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

.data-row.selected {
  background-color: #ecf5ff;
}

/* 列宽定义 */
.col-checkbox {
  width: 40px;
  padding: 12px 8px;
  text-align: center;
}

.col-image {
  width: 80px;
  padding: 8px;
  text-align: center;
}

.col-id {
  width: 70px;
  padding: 12px 8px;
}

.col-name {
  width: 200px;
  padding: 12px 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-type {
  width: 120px;
  padding: 12px 8px;
}

.col-publisher {
  width: 120px;
  padding: 12px 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-status {
  width: 100px;
  padding: 12px 8px;
}

.col-actions {
  width: 150px;
  padding: 8px;
  text-align: center;
}

/* 图片样式 */
.item-image {
  cursor: pointer;
  transition: transform 0.2s;
}

.item-image:hover {
  transform: scale(1.05);
}

.item-image img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.no-image {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
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

/* 图片预览模态框 */
.image-preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.preview-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.preview-content img {
  max-width: 100%;
  max-height: 90vh;
  border-radius: 8px;
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 30px;
  cursor: pointer;
  width: 40px;
  height: 40px;
}

.close-btn:hover {
  color: #f56c6c;
}

/* 复选框样式 */
input[type="checkbox"] {
  cursor: pointer;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .simple-table {
    font-size: 14px;
  }

  .col-name {
    width: 150px;
  }

  .col-type, .col-publisher {
    width: 100px;
  }
}

@media (max-width: 768px) {
  .admin-item-audit {
    padding: 10px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar > * {
    margin-bottom: 10px;
  }

  .batch-operation {
    flex-direction: column;
    gap: 15px;
  }

  .batch-btn-group {
    justify-content: center;
  }

  .simple-table {
    overflow-x: auto;
  }

  .table-row {
    min-width: 800px;
  }
}
</style>