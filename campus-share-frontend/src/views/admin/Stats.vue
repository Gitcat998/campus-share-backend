<template>
  <div class="admin-stats-container">
    <el-card title="平台数据统计">
      <!-- 核心指标核心指标卡片 -->
      <div class="stats-summary">
        <el-card class="summary-card">
          <div class="summary-label">总物品数</div>
          <div class="summary-value">{{ totalItems }}</div>
        </el-card>
        <el-card class="summary-card">
          <div class="summary-label">待审核物品</div>
          <div class="summary-value">{{ pendingItems }}</div>
        </el-card>
        <el-card class="summary-card">
          <div class="summary-label">可借用物品</div>
          <div class="summary-value">{{ availableItems }}</div>
        </el-card>
        <el-card class="summary-card">
          <div class="summary-label">总评价数</div>
          <div class="summary-value">{{ totalComments }}</div>
        </el-card>
      </div>

      <!-- 图表区域 -->
      <div class="charts-grid">
        <!-- 物品状态分布饼图 -->
        <el-card class="chart-card">
          <div slot="header">物品状态分布</div>
          <div class="chart-container">
            <canvas id="statusChart"></canvas>
          </div>
        </el-card>

        <!-- 分类物品数量柱状图 -->
        <el-card class="chart-card">
          <div slot="header">分类物品数量</div>
          <div class="chart-container">
            <canvas id="categoryChart"></canvas>
          </div>
        </el-card>

        <!-- 评价统计柱状图（若有数据） -->
        <el-card class="chart-card" v-if="commentStats.length > 0">
          <div slot="header">物品评价统计</div>
          <div class="chart-container">
            <canvas id="commentChart"></canvas>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getStatsOverview } from '@/api/stats'
import Chart from 'chart.js'
import { Message } from 'element-ui'

export default {
  name: 'AdminStats',
  data() {
    return {
      // 统计数据
      statsOverview: null,
      // 计算属性依赖的数据
      itemStatusStats: [],
      categoryItemStats: [],
      commentStats: []
    }
  },
  computed: {
    // 总物品数（各状态数量之和）
    totalItems() {
      return this.itemStatusStats.reduce((sum, item) => sum + (item.count || 0), 0)
    },
    // 待审核物品数
    pendingItems() {
      return this.itemStatusStats.find(item => item.status === 0)?.count || 0
    },
    // 可借用物品数
    availableItems() {
      return this.itemStatusStats.find(item => item.status === 1)?.count || 0
    },
    // 总评价数
    totalComments() {
      return this.commentStats.reduce((sum, item) => sum + (item.commentCount || 0), 0)
    }
  },
  created() {
    this.loadStatsData()
  },
  methods: {
    // 加载统计数据
    async loadStatsData() {
      try {
        const res = await getStatsOverview()
        if (res.code === 200) {
          this.statsOverview = res.data
          this.itemStatusStats = res.data.itemStatusStats || []
          this.categoryItemStats = res.data.categoryItemStats || []
          this.commentStats = res.data.commentStats || []
          // 绘制图表
          this.initCharts()
        } else {
          Message.error('获取统计数据失败：' + res.msg)
        }
      } catch (error) {
        console.error('统计数据加载失败：', error)
        Message.error('网络异常，无法加载统计数据')
      }
    },

    // 初始化所有图表
    initCharts() {
      this.drawStatusChart()
      this.drawCategoryChart()
      if (this.commentStats.length > 0) {
        this.drawCommentChart()
      }
    },

    // 物品状态分布饼图
    drawStatusChart() {
      const ctx = document.getElementById('statusChart').getContext('2d')
      new Chart(ctx, {
        type: 'pie',
        data: {
          labels: this.itemStatusStats.map(item => item.statusName),
          datasets: [{
            data: this.itemStatusStats.map(item => item.count),
            backgroundColor: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'],
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'right'
            }
          }
        }
      })
    },

    // 分类物品数量柱状图
    drawCategoryChart() {
      const ctx = document.getElementById('categoryChart').getContext('2d')
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.categoryItemStats.map(item => item.categoryName),
          datasets: [{
            label: '物品数量',
            data: this.categoryItemStats.map(item => item.count),
            backgroundColor: '#409eff',
            borderRadius: 4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true,
              ticks: { precision: 0 } // 确保y轴为整数
            }
          }
        }
      })
    },

    // 评价统计柱状图
    drawCommentChart() {
      const ctx = document.getElementById('commentChart').getContext('2d')
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.commentStats.map(item => item.itemName),
          datasets: [{
            label: '评价数量',
            data: this.commentStats.map(item => item.commentCount),
            backgroundColor: '#67c23a',
            borderRadius: 4,
            yAxisID: 'y'
          }, {
            label: '平均评分',
            data: this.commentStats.map(item => item.avgScore),
            backgroundColor: '#f56c6c',
            type: 'line',
            yAxisID: 'y1'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true,
              position: 'left',
              title: { display: true, text: '评价数量' }
            },
            y1: {
              beginAtZero: true,
              position: 'right',
              max: 5, // 评分最高5分
              title: { display: true, text: '平均评分' },
              grid: { drawOnChartArea: false } // 不绘制网格线，避免与y轴冲突
            }
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.admin-stats-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.stats-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  flex: 1;
  min-width: 200px;
  text-align: center;
  padding: 20px 10px;
}

.summary-label {
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 20px;
}

.chart-card {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex: 1;
  width: 100%;
  padding: 10px;
}
</style>