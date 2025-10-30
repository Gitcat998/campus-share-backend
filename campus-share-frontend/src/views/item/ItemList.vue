<template>
  <div class="item-list-container">
    <!-- 筛选区 -->
    <el-card class="filter-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="filters.itemTypeId" placeholder="物品分类">
            <!-- 新增：全部分类选项（value为空表示不筛选） -->
            <el-option label="全部分类" value=""></el-option>
            <el-option
                v-for="type in itemTypes"
                :key="type.id"
                :label="type.typeName"
                :value="type.id"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="filters.status" placeholder="物品状态">
            <el-option label="全状态" value=""></el-option> <!-- 补充全状态选项 -->
            <el-option label="待审核" value="0"></el-option>
            <el-option label="可借用" value="1"></el-option>
            <el-option label="已借出" value="2"></el-option>
            <el-option label="已下架" value="3"></el-option>
          </el-select>
        </el-col>
        <el-col :span="10">
          <el-input
              v-model="filters.name"
              placeholder="请输入物品名称搜索"
              clearable
              @keyup.enter.native="getItemList"
          >
            <el-button slot="append" icon="el-icon-search" @click="getItemList"></el-button>
          </el-input>
        </el-col>
        <el-col :span="2">
          <el-button type="primary" @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 物品列表 -->
    <div class="item-grid">
      <el-card
          v-for="item in itemList"
          :key="item.id"
          class="item-card"
          @click.native="goToDetail(item.id)"
      >
        <!-- 物品主图 -->
        <div class="item-image">
          <img :src="item.mainImageUrl || require('../../assets/images/default.png')" alt="物品图片">
        </div>
        <!-- 物品信息 -->
        <div class="item-info">
          <h3 class="item-name">{{ item.name }}</h3>
          <p class="item-type">分类：{{ item.typeName }}</p>
          <p class="item-publisher">发布者：{{ item.publisherName }}</p>
          <el-tag
              :type="getStatusType(item.status)"
              class="item-status"
          >
            {{ getStatusText(item.status) }}
          </el-tag>
        </div>
      </el-card>
    </div>

    <!-- 空状态处理 -->
    <div v-if="itemList.length === 0 && total === 0" class="empty-state">
      <el-empty description="暂无符合条件的物品"></el-empty>
    </div>

    <!-- 分页 -->
    <el-pagination
        class="pagination"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="filters.pageSize"
        :current-page="filters.pageNum"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    ></el-pagination>
  </div>
</template>

<script>
import { getItemList, getItemTypes } from '../../api/item'

export default {
  name: 'ItemList',
  data() {
    return {
      itemList: [], // 物品列表
      itemTypes: [], // 分类列表（用于筛选）
      total: 0, // 总条数
      filters: {
        pageNum: 1,
        pageSize: 10,
        name: '', // 物品名称搜索
        itemTypeId: '', // 分类筛选（空表示全部分类）
        status: '' // 状态筛选（空表示全状态）
      }
    }
  },
  mounted() {
    // 页面加载时获取数据
    this.getItemTypes()
    this.getItemList()
  },
  methods: {
    // 获取分类列表（处理树形结构）
    async getItemTypes() {
      try {
        const res = await getItemTypes()
        if (res.code === 200 && res.data) {
          this.itemTypes = this.flattenTree(res.data)
        } else {
          this.$message.error('获取分类失败：' + (res.msg || '数据格式错误'))
        }
      } catch (error) {
        console.error('获取分类接口错误详情：', error)
        this.$message.error('获取分类失败')
      }
    },
    // 递归扁平化树形结构
    flattenTree(tree) {
      let result = []
      tree.forEach(node => {
        // 添加当前节点
        result.push({
          id: node.id,
          typeName: node.typeName
        })
        // 递归处理子节点
        if (node.children && node.children.length > 0) {
          result = result.concat(this.flattenTree(node.children))
        }
      })
      return result
    },
    // 获取物品列表
    async getItemList() {
      try {
        const res = await getItemList(this.filters);
        console.log('物品列表接口响应：', res);

        if (res.code === 200 && res.data) {
          this.itemList = res.data.records || [];
          this.total = res.data.total || res.data.records.length;
        } else {
          this.$message.error('获取物品列表失败：' + (res.msg || '数据格式错误'));
          this.itemList = [];
          this.total = 0;
        }
      } catch (error) {
        console.error('物品列表接口调用失败：', error);
        this.itemList = [];
        this.total = 0;
        this.$message.error('获取物品列表失败');
      }
    },
    // 跳转到物品详情页
    goToDetail(itemId) {
      this.$router.push(`/items/${itemId}`)
    },
    // 重置筛选条件
    resetFilter() {
      this.filters = {
        ...this.filters,
        name: '',
        itemTypeId: '', // 重置为全部分类
        status: '' // 重置为全状态
      }
      this.getItemList()
    },
    // 分页大小变化
    handleSizeChange(size) {
      this.filters.pageSize = size
      this.getItemList()
    },
    // 当前页变化
    handleCurrentChange(page) {
      this.filters.pageNum = page
      this.getItemList()
    },
    // 状态文本转换
    getStatusText(status) {
      const statusMap = {
        0: '待审核',
        1: '可借用',
        2: '已借出',
        3: '已下架'
      }
      return statusMap[status] || '未知'
    },
    // 状态标签样式
    getStatusType(status) {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'info',
        3: 'danger'
      }
      return typeMap[status] || 'default'
    }
  }
}
</script>

<style scoped>
.item-list-container {
  padding: 20px;
}
.filter-card {
  margin-bottom: 20px;
  padding: 15px;
}
.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}
.item-card {
  cursor: pointer;
  transition: transform 0.3s;
}
.item-card:hover {
  transform: translateY(-5px);
}
.item-image {
  height: 200px;
  overflow: hidden;
}
.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.item-info {
  padding: 15px;
}
.item-name {
  font-size: 16px;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-type, .item-publisher {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}
.item-status {
  margin-top: 10px;
}
.pagination {
  text-align: right;
  margin-top: 20px;
}
/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 50px 0;
}
</style>