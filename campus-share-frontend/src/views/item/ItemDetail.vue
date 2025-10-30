<template>
  <div class="item-detail-container">
    <!-- 物品详情卡片，带加载状态 -->
    <el-card v-loading="loading" element-loading-text="加载中...">
      <!-- 图片轮播 + 基本信息 -->
      <el-row :gutter="20">
        <!-- 左侧图片轮播 -->
        <el-col :span="10">
          <el-carousel height="400px" indicator-position="outside" v-if="item.imageUrls && item.imageUrls.length > 0">
            <el-carousel-item v-for="(url, index) in item.imageUrls" :key="index">
              <img
                  :src="resolveImgUrl(url)"
                  alt="物品图片"
                  class="carousel-img"
                  @error="handleImgError($event)"
              >
            </el-carousel-item>
          </el-carousel>
          <!-- 无图片时显示默认图 -->
          <div v-else class="default-img-container">
            <img
                src="~@/assets/images/default.png"
                alt="默认图片"
                class="default-img"
            >
          </div>
        </el-col>

        <!-- 右侧基本信息 -->
        <el-col :span="14">
          <div class="item-basic-info">
            <h2 class="item-name">{{ item.name || '未知物品' }}</h2>
            <el-tag
                :type="getStatusType(item.status)"
                class="item-status-tag"
            >
              {{ getStatusText(item.status) }}
            </el-tag>

            <!-- 分类信息 -->
            <div class="info-row">
              <span class="info-label">分类：</span>
              <span>{{ item.typeName || '未分类' }}</span>
            </div>

            <!-- 可借时长 -->
            <div class="info-row">
              <span class="info-label">可借时长：</span>
              <span>{{ item.borrowDuration || 0 }} 天</span>
            </div>

            <!-- 发布者信息 -->
            <div class="info-row">
              <span class="info-label">发布者：</span>
              <span>{{ item.publisherName || '未知用户' }}</span>
            </div>

            <!-- 发布时间 -->
            <div class="info-row">
              <span class="info-label">发布时间：</span>
              <span>{{ formatTime(item.createTime) || '未知时间' }}</span>
            </div>

            <!-- 物品描述 -->
            <div class="info-row">
              <span class="info-label">物品描述：</span>
              <p class="item-desc">{{ item.description || '暂无描述' }}</p>
            </div>

            <!-- 操作按钮（移除编辑按钮） -->
            <div class="action-buttons" v-if="Object.keys(item).length > 0">
              <!-- 普通用户：可借用时显示申请按钮 -->
              <el-button
                  type="primary"
                  @click="goToApply"
                  v-if="isNormalUser && item.status === 1"
              >
                申请借用
              </el-button>

              <!-- 发布者：仅保留下架按钮（移除编辑按钮） -->
              <el-button
                  type="warning"
                  @click="handleOffline"
                  v-if="isPublisher && item.status !== 3"
              >
                下架物品
              </el-button>

              <!-- 管理员：审核按钮 -->
              <el-button
                  type="success"
                  @click="handleAudit(1)"
                  v-if="isAdmin && item.status === 0"
              >
                审核通过
              </el-button>
              <el-button
                  type="danger"
                  @click="handleAudit(3)"
                  v-if="isAdmin && item.status === 0"
              >
                审核拒绝
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 评价列表区域 -->
      <div class="comments-section">
        <h3>用户评价</h3>
        <el-divider></el-divider>

        <!-- 有评价时显示列表 -->
        <el-list v-if="comments.length > 0" item-layout="horizontal">
          <el-list-item v-for="comment in comments" :key="comment.id">
            <el-list-item__avatar>
              <img
                  :src="comment.userAvatar ? resolveImgUrl(comment.userAvatar) : require('@/assets/images/default.png')"
                  alt="用户头像"
                  class="avatar-img"
              >
            </el-list-item__avatar>
            <el-list-item__main>
              <div class="comment-user">{{ comment.userName || '匿名用户' }}</div>
              <div class="comment-time">{{ formatTime(comment.createTime) || '未知时间' }}</div>
              <div class="comment-content">{{ comment.content }}</div>
              <el-rate
                  :value="comment.score || 0"
                  disabled
                  show-text
                  class="comment-score"
              ></el-rate>
            </el-list-item__main>
          </el-list-item>
        </el-list>

        <!-- 无评价时显示提示 -->
        <div v-else class="no-comments">暂无评价</div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getItemDetail, getComments, auditItemPass, offlineItem } from '@/api/item'
import { formatTime } from '@/utils/format';
import { mapState } from 'vuex'
const BASE_API = 'http://localhost:8080/api/v1'  // 后端基础路径配置

export default {
  name: 'ItemDetail',
  data() {
    return {
      itemId: this.$route.params.id, // 从路由获取物品ID
      item: {}, // 物品详情数据
      comments: [], // 评价列表
      loading: true, // 加载状态
      loadTimeout: null // 加载超时计时器
    }
  },
  computed: {
    ...mapState('user', ['userInfo', 'role']),

    // 判断是否为发布者（匹配后端userId字段）
    isPublisher() {
      return this.userInfo?.id && this.item.userId === this.userInfo.id
    },

    // 判断是否为普通用户（非管理员且非发布者）
    isNormalUser() {
      return this.userInfo?.id && !this.isPublisher && this.role !== '管理员'
    },

    // 判断是否为管理员（后端角色为ADMIN）
    isAdmin() {
      return this.role === '管理员'
    }
  },
  mounted() {
    // 校验物品ID是否为有效数字
    if (!this.itemId || isNaN(Number(this.itemId))) {
      this.$message.error('物品ID无效')
      this.$router.push('/item/list')
      return
    }
    this.loadItemData()
  },
  beforeDestroy() {
    // 清除超时计时器
    if (this.loadTimeout) clearTimeout(this.loadTimeout)
  },
  methods: {
    formatTime,

    // 处理图片URL（拼接基础路径）
    resolveImgUrl(url) {
      if (!url) return ''
      // 若为绝对路径则直接返回，否则拼接基础路径
      return url.startsWith('http') ? url : `${BASE_API}${url}`
    },

    // 加载物品详情和评价（添加超时控制）
    async loadItemData() {
      try {
        this.loading = true
        // 超时控制（15秒未加载完成则提示）
        this.loadTimeout = setTimeout(() => {
          this.$message.warning('数据加载超时，请稍后重试')
        }, 15000)

        // 校验用户信息是否加载完成
        if (!this.userInfo) {
          this.$message.warning('请先登录')
          return
        }

        // 并行请求数据
        const [itemRes, commentsRes] = await Promise.all([
          getItemDetail(this.itemId),
          getComments(this.itemId)
        ])

        // 处理物品详情
        if (itemRes.code === 200) {
          this.item = itemRes.data || {}
        } else {
          this.$message.error('获取物品详情失败：' + itemRes.msg)
        }

        // 处理评价列表
        if (commentsRes.code === 200) {
          this.comments = commentsRes.data.list || commentsRes.data || []
        } else {
          this.$message.warning('获取评价失败：' + commentsRes.msg)
          this.comments = []
        }

        // 开发环境打印调试日志
        if (process.env.NODE_ENV === 'development') {
          console.log('物品详情数据：', this.item)
          console.log('用户权限信息：', {
            isLogin: !!this.userInfo?.id,
            isPublisher: this.isPublisher,
            isAdmin: this.isAdmin
          })
        }

      } catch (error) {
        console.error('加载数据失败：', error)
        this.$message.error('加载物品信息失败，请重试')
      } finally {
        this.loading = false
        clearTimeout(this.loadTimeout) // 清除超时计时器
      }
    },

    // 图片加载失败显示默认图
    handleImgError(e) {
      e.target.src = require('@/assets/images/default.png')
    },

    // 状态文本转换
    getStatusText(status) {
      const statusMap = {0: '待审核', 1: '可借用', 2: '已借出', 3: '已下架'}
      return statusMap[status] || '未知状态'
    },

    // 状态标签样式
    getStatusType(status) {
      const typeMap = {0: 'warning', 1: 'success', 2: 'info', 3: 'danger'}
      return typeMap[status] || 'default'
    },

    // 跳转到借用申请页（添加物品ID校验）
    goToApply() {
      console.log('跳转申请借用页面，itemId:', this.itemId);
      this.$router.push({
        name: 'BorrowApply', // 使用路由名称
        params: { itemId: this.itemId } // 通过 params 传递
      });
    },

    // 审核/下架操作
    async handleAudit(status) {
      const confirmText = status === 1
          ? '确定要审核通过该物品吗？'
          : '确定要下架/拒绝该物品吗？'

      this.$confirm(confirmText, '操作确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          let res
          if (status === 1) {
            res = await auditItemPass(this.itemId) // 审核通过
          } else if (status === 3) {
            res = await offlineItem(this.itemId) // 下架/拒绝
          }

          if (res?.code === 200) {
            this.$message.success('操作成功')
            this.loadItemData() // 刷新数据
          } else {
            this.$message.error('操作失败：' + (res?.msg || '未知错误'))
          }
        } catch (error) {
          console.error('状态更新失败：', error)
          this.$message.error('操作失败，请重试')
        }
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    },

    // 发布者下架物品
    handleOffline() {
      this.handleAudit(3)
    }
  }
}
</script>

<style scoped>
.item-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 图片轮播样式 */
.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: #f5f5f5;
}

.default-img-container {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
}

.default-img {
  width: 200px;
  height: 200px;
  object-fit: contain;
}

/* 基本信息样式 */
.item-basic-info {
  padding: 10px 0;
}

.item-name {
  margin-bottom: 15px;
  font-size: 22px;
  color: #333;
}

.item-status-tag {
  margin-bottom: 20px;
}

.info-row {
  margin: 15px 0;
  line-height: 1.8;
}

.info-label {
  display: inline-block;
  width: 80px;
  color: #666;
  font-weight: 500;
}

.item-desc {
  margin: 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
  color: #555;
}

/* 操作按钮样式 */
.action-buttons {
  margin-top: 30px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 评价区域样式 */
.comments-section {
  margin-top: 40px;
  padding: 20px 0;
}

.comments-section h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-user {
  font-weight: bold;
  margin-bottom: 5px;
  color: #333;
}

.comment-time {
  color: #999;
  font-size: 12px;
  margin-bottom: 10px;
}

.comment-content {
  margin-bottom: 10px;
  line-height: 1.6;
  color: #555;
}

.comment-score {
  margin-top: 5px;
}

.no-comments {
  text-align: center;
  padding: 30px;
  color: #999;
  background-color: #f9f9f9;
  border-radius: 4px;
}
</style>