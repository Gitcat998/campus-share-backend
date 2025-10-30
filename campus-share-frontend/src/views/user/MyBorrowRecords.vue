<template>
  <div class="my-records-container">
    <el-card title="我的借用记录" class="records-card">
      <!-- 状态筛选 -->
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" @change="handleFilterChange">
          <el-radio :label="''">全部记录</el-radio>
          <el-radio :label="1">进行中</el-radio>
          <el-radio :label="2">已归还</el-radio>
        </el-radio-group>
        <el-button type="text" @click="fetchRecords">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>

      <!-- 借用记录列表 -->
      <el-table
          v-loading="loading"
          :data="recordsList"
          border
          style="width: 100%; margin-top: 15px;"
      >
        <el-table-column prop="itemName" label="物品名称" width="200">
          <template slot-scope="scope">
            <span @click="goToItemDetail(scope.row.itemId)" class="item-name-link">
              {{ scope.row.itemName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180"></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180"></el-table-column>
        <el-table-column prop="endTime" label="预计结束时间" width="180"></el-table-column>
        <el-table-column prop="actualReturnTime" label="实际归还时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.actualReturnTime || '未归还' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template slot-scope="scope">
            <el-tag
                :type="scope.row.status === 1 ? 'info' : 'success'"
            >
              {{ scope.row.status === 1 ? '进行中' : '已归还' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button
                type="text"
                @click="showRecordDetail(scope.row)"
                size="small"
            >
              详情
            </el-button>
            <!-- 进行中状态显示“确认归还”按钮 -->
            <el-button
                type="primary"
                @click="confirmReturn(scope.row)"
                size="small"
                v-if="scope.row.status === 1"
            >
              确认归还
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSize"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
        ></el-pagination>
      </div>

      <!-- 无数据提示 -->
      <div v-if="!loading && recordsList.length === 0 && total === 0" class="no-data">
        <el-empty description="暂无借用记录"></el-empty>
      </div>
    </el-card>

    <!-- 记录详情弹窗 -->
    <el-dialog
        :visible.sync="detailVisible"
        title="借用详情"
        width="60%"
    >
      <el-descriptions column="1" border>
        <el-descriptions-item label="物品名称">{{ currentRecord.itemName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRecord.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审核通过时间">{{ currentRecord.auditTime }}</el-descriptions-item>
        <el-descriptions-item label="借用时间">
          {{ currentRecord.startTime }} 至 {{ currentRecord.endTime }}
        </el-descriptions-item>
        <el-descriptions-item label="实际归还时间" v-if="currentRecord.actualReturnTime">
          {{ currentRecord.actualReturnTime }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRecord.status === 1 ? 'info' : 'success'">
            {{ currentRecord.status === 1 ? '进行中' : '已归还' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getMyBorrowRecords, confirmReturnItem } from '@/api/borrowApply'
import { formatTime } from '@/utils/format'

export default {
  name: 'MyBorrowRecords',
  data() {
    return {
      loading: false,
      recordsList: [], // 借用记录列表
      statusFilter: '', // 筛选：''=全部，1=进行中，2=已归还
      detailVisible: false,
      currentRecord: {}, // 当前查看的记录
      // 分页参数
      currentPage: 1, // 当前页码
      pageSize: 10, // 每页条数
      total: 0 // 总记录数
    }
  },
  created() {
    this.fetchRecords()
  },
  methods: {
    // 1. 获取借用记录（适配分页接口）
    async fetchRecords() {
      try {
        this.loading = true
        // 构建请求参数：包含筛选条件和分页参数
        const params = {
          status: this.statusFilter,
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }
        const res = await getMyBorrowRecords(params)

        // 打印后端返回数据，便于调试
        console.log('后端返回的借用记录数据：', res)

        if (res.code === 200) {
          // 从分页对象的 records 字段获取数组（核心修复）
          const pageData = res.data || {}
          const records = Array.isArray(pageData.records) ? pageData.records : []

          // 格式化时间并赋值
          this.recordsList = records.map(record => ({
            ...record,
            applyTime: formatTime(record.applyTime),
            auditTime: formatTime(record.auditTime),
            startTime: formatTime(record.startTime),
            endTime: formatTime(record.endTime),
            actualReturnTime: record.actualReturnTime ? formatTime(record.actualReturnTime) : ''
          }))

          // 更新总条数（用于分页）
          this.total = pageData.total || 0
        } else {
          this.$message.error('获取借用记录失败：' + (res.msg || '未知错误'))
          this.recordsList = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取记录异常：', error)
        this.$message.error('网络异常，请重试')
        this.recordsList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    // 2. 筛选条件变化时，重置页码并重新查询
    handleFilterChange() {
      this.currentPage = 1 // 筛选条件变了，从第1页开始查
      this.fetchRecords()
    },

    // 3. 分页：每页条数变化
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.currentPage = 1 // 页码重置为1
      this.fetchRecords()
    },

    // 4. 分页：当前页码变化
    handleCurrentChange(currentPage) {
      this.currentPage = currentPage
      this.fetchRecords()
    },

    // 5. 跳转到物品详情
    goToItemDetail(itemId) {
      this.$router.push(`/items/${itemId}`)
    },

    // 6. 查看记录详情
    showRecordDetail(record) {
      this.currentRecord = { ...record } // 深拷贝，避免修改原数据
      this.detailVisible = true
    },

    // 7. 确认归还物品
    async confirmReturn(record) {
      this.$confirm('确定已归还该物品吗？', '确认归还', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await confirmReturnItem(record.id)
          if (res.code === 200) {
            this.$message.success('确认归还成功')
            this.fetchRecords() // 刷新列表
            this.detailVisible = false // 关闭详情弹窗（如果打开）
          } else {
            this.$message.error(res.msg || '操作失败')
          }
        } catch (error) {
          console.error('确认归还异常：', error)
          this.$message.error('网络异常，请重试')
        }
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    }
  }
}
</script>

<style scoped>
.my-records-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.records-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  padding-bottom: 20px; /* 预留分页空间 */
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 0 15px;
}

.item-name-link {
  color: #409eff;
  cursor: pointer;
}

.item-name-link:hover {
  text-decoration: underline;
}

.no-data {
  margin: 50px 0;
  text-align: center;
}

.pagination-container {
  margin-top: 15px;
  text-align: right;
  padding-right: 15px;
}
</style>