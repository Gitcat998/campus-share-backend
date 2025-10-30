<template>
  <div class="admin-stats-container">
    <el-card title="平台数据统计" class="main-card">
      <!-- 核心指标卡片 -->
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

      <!-- 图表区域：响应式网格布局 -->
      <div class="charts-container">
        <!-- 物品状态分布饼图 -->
        <el-card class="chart-card status-chart">
          <div slot="header">物品状态分布</div>
          <div class="chart-wrapper">
            <canvas id="statusChart"></canvas>
          </div>
        </el-card>

        <!-- 分类物品数量柱状图 -->
        <el-card class="chart-card category-chart">
          <div slot="header">分类物品数量</div>
          <div class="chart-wrapper">
            <canvas id="categoryChart"></canvas>
          </div>
        </el-card>

        <!-- 评价统计柱状图 -->
        <el-card class="chart-card comment-chart">
          <div slot="header">物品评价统计</div>
          <div class="chart-wrapper" v-if="commentStats.length > 0">
            <canvas id="commentChart"></canvas>
          </div>
          <div class="empty-hint" v-else>暂无评价数据</div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getStatsOverview } from '@/api/stats'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)
import { Message } from 'element-ui'

export default {
  name: 'AdminStats',
  data() {
    return {
      itemStatusStats: [],
      categoryItemStats: [],
      commentStats: [],
      statusChartInstance: null,
      categoryChartInstance: null,
      commentChartInstance: null
    }
  },
  computed: {
    totalItems() {
      return this.itemStatusStats.reduce((sum, item) => sum + (item.count || 0), 0)
    },
    pendingItems() {
      return this.itemStatusStats.find(item => item.status === 0)?.count || 0
    },
    availableItems() {
      return this.itemStatusStats.find(item => item.status === 1)?.count || 0
    },
    totalComments() {
      return this.commentStats.reduce((sum, item) => sum + (item.commentCount || 0), 0)
    }
  },
  created() {
    this.loadStatsData()
  },
  methods: {
    async loadStatsData() {
      try {
        const res = await getStatsOverview()
        if (res.code === 200) {
          this.itemStatusStats = res.data.itemStatusStats || []
          this.categoryItemStats = res.data.categoryItemStats || []
          this.commentStats = res.data.commentStats || []
          this.$nextTick(() => this.initCharts())
        } else {
          Message.error('获取统计数据失败：' + res.msg)
        }
      } catch (error) {
        console.error('统计数据加载失败：', error)
        Message.error('网络异常，无法加载统计数据')
      }
    },
    initCharts() {
      this.drawStatusChart()
      this.drawCategoryChart()
      if (this.commentStats.length > 0) this.drawCommentChart()
    },
    drawStatusChart() {
      const ctx = document.getElementById('statusChart').getContext('2d')
      if (this.statusChartInstance) this.statusChartInstance.destroy()
      this.statusChartInstance = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: this.itemStatusStats.map(item => item.statusName),
          datasets: [{
            data: this.itemStatusStats.map(item => item.count || 0),
            backgroundColor: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'],
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { position: 'right' } }
        }
      })
    },
    drawCategoryChart() {
      const ctx = document.getElementById('categoryChart').getContext('2d')
      if (this.categoryChartInstance) this.categoryChartInstance.destroy()
      this.categoryChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.categoryItemStats.map(item => item.categoryName),
          datasets: [{
            label: '物品数量',
            data: this.categoryItemStats.map(item => item.itemCount || 0),
            backgroundColor: '#409eff',
            borderRadius: 4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
      })
    },
    drawCommentChart() {
      const ctx = document.getElementById('commentChart').getContext('2d')
      if (this.commentChartInstance) this.commentChartInstance.destroy()
      this.commentChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.commentStats.map(item => item.itemName),
          datasets: [{
            label: '评价数量',
            data: this.commentStats.map(item => item.commentCount || 0),
            backgroundColor: '#67c23a',
            borderRadius: 4,
            yAxisID: 'y'
          }, {
            label: '平均评分',
            data: this.commentStats.map(item => item.avgScore || 0),
            backgroundColor: '#f56c6c',
            type: 'line',
            yAxisID: 'y1'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {beginAtZero: true, position: 'left', title: {display: true, text: '评价数量'}},
            y1: {
              beginAtZero: true,
              position: 'right',
              max: 5,
              title: {display: true, text: '平均评分'},
              grid: {drawOnChartArea: false}
            }
          }
        }
      })
    }
  },
  beforeDestroy() {
    if (this.statusChartInstance) this.statusChartInstance.destroy()
    if (this.categoryChartInstance) this.categoryChartInstance.destroy()
    if (this.commentChartInstance) this.commentChartInstance.destroy()
  }
}
</script>

<style scoped>
/* 容器样式 */
.admin-stats-container {
  padding: 20px;
  max-width: 1600px; /* 限制最大宽度，避免大屏过宽 */
  margin: 0 auto;
  background-color: #f5f7fa; /* 浅灰背景，突出卡片 */
}

/* 主卡片样式 */
.main-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 轻微阴影，增强层次感 */
  overflow: hidden;
}

/* 核心指标区域 */
.stats-summary {
  display: flex;
  gap: 16px;
  padding: 10px 20px; /* 内边距调整 */
  flex-wrap: wrap; /* 小屏幕自动换行 */
}

/* 指标卡片样式 */
.summary-card {
  flex: 1;
  min-width: 200px;
  max-width: 300px; /* 限制最大宽度，避免卡片过宽 */
  border-radius: 6px;
  transition: transform 0.2s; /* 悬停效果 */
  border: none; /* 去掉默认边框 */
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.summary-card:hover {
  transform: translateY(-3px); /* 轻微上浮，增强交互感 */
}

.summary-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}

.summary-value {
  font-size: 26px;
  font-weight: bold;
  color: #2c3e50; /* 深灰主色，更专业 */
  line-height: 1.2;
}

/* 图表容器：响应式网格 */
.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr)); /* 自适应列数 */
  gap: 20px;
  padding: 10px 20px 20px; /* 内边距调整 */
}

/* 图表卡片通用样式 */
.chart-card {
  border-radius: 6px;
  border: none;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 不同图表高度调整（根据内容适配） */
.status-chart {
  height: 400px; /* 饼图高度适中 */
}

.category-chart, .comment-chart {
  height: 450px; /* 柱状图需要更高空间 */
}

/* 图表标题样式 */
::v-deep .el-card__header {
  padding: 12px 20px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

::v-deep .el-card__header div {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

/* 图表绘制区域 */
.chart-wrapper {
  flex: 1;
  width: 100%;
  padding: 20px; /* 增加内边距，让图表不贴边 */
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 空数据提示 */
.empty-hint {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}

/* 响应式调整：小屏幕适配 */
@media (max-width: 1000px) {
  .charts-container {
    grid-template-columns: 1fr; /* 小屏幕单列显示 */
  }

  .status-chart, .category-chart, .comment-chart {
    height: 350px; /* 小屏幕降低高度 */
  }

  .summary-card {
    min-width: 150px;
  }

  .summary-value {
    font-size: 22px;
  }
}
</style>