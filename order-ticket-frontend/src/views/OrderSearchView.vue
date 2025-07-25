<template>
  <div class="search-container">
    <div class="search-header">
      <h2>订单查询</h2>
    </div>

    <el-card class="search-form-card">
      <div class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="订单号">
              <el-input
                v-model="searchParams.orderNumber"
                placeholder="请输入订单号"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="客户姓名">
              <el-input
                v-model="searchParams.customerName"
                placeholder="请输入客户姓名"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单状态">
              <el-select
                v-model="searchParams.status"
                placeholder="请选择状态"
                clearable
                style="width: 100%"
              >
                <el-option label="待处理" value="PENDING" />
                <el-option label="已确认" value="CONFIRMED" />
                <el-option label="处理中" value="PROCESSING" />
                <el-option label="已发货" value="SHIPPED" />
                <el-option label="已送达" value="DELIVERED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                搜索
              </el-button>
              <el-button @click="handleReset">
                重置
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card class="results-card">
      <template #header>
        <div class="results-header">
          <span>搜索结果</span>
          <span v-if="searchResults.length > 0">
            共找到 {{ totalElements }} 条记录
          </span>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton animated :rows="5" />
      </div>

      <div v-else-if="searchResults.length === 0" class="empty-container">
        <el-empty description="暂无搜索结果" />
      </div>

      <div v-else>
        <el-table
          :data="searchResults"
          class="search-table"
          stripe
          @row-click="viewOrderDetail"
        >
          <el-table-column prop="orderNumber" label="订单号" width="160">
            <template #default="{ row }">
              <span class="order-number">{{ row.orderNumber }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="customerName" label="客户姓名" width="120" />
          <el-table-column prop="customerEmail" label="客户邮箱" width="200" />
          <el-table-column prop="totalAmount" label="订单金额" width="120">
            <template #default="{ row }">
              <span class="amount">¥{{ formatAmount(row.totalAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="orderDate" label="订单日期" width="160">
            <template #default="{ row }">
              {{ formatDate(row.orderDate) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click.stop="viewOrderDetail(row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalElements"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi, type Order } from '@/api/orderApi'
import { ElMessage } from 'element-plus'

const router = useRouter()

const searchParams = reactive({
  orderNumber: '',
  customerName: '',
  status: ''
})

const searchResults = ref<Order[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)

const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': 'warning',
    'CONFIRMED': 'info',
    'PROCESSING': 'primary',
    'SHIPPED': 'success',
    'DELIVERED': 'success',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '待处理',
    'CONFIRMED': '已确认',
    'PROCESSING': '处理中',
    'SHIPPED': '已发货',
    'DELIVERED': '已送达',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const formatAmount = (amount: number | string) => {
  return Number(amount).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const handleSearch = async () => {
  if (!searchParams.orderNumber && !searchParams.customerName && !searchParams.status) {
    ElMessage.warning('请至少输入一个搜索条件')
    return
  }

  loading.value = true
  try {
    const response = await orderApi.searchOrders({
      ...searchParams,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    searchResults.value = response.content
    totalElements.value = response.totalElements
    
    if (searchResults.value.length === 0) {
      ElMessage.info('未找到匹配的订单')
    }
  } catch (error) {
    console.error('Search failed:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchParams.orderNumber = ''
  searchParams.customerName = ''
  searchParams.status = ''
  searchResults.value = []
  totalElements.value = 0
  currentPage.value = 1
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
  handleSearch()
}

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
  handleSearch()
}

const viewOrderDetail = (row: Order) => {
  router.push(`/orders/${row.id}`)
}
</script>

<style scoped>
.search-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  margin-bottom: 20px;
}

.search-header h2 {
  margin: 0;
  color: #303133;
}

.search-form-card {
  margin-bottom: 20px;
}

.search-form {
  padding: 20px;
}

.results-card {
  min-height: 400px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container,
.empty-container {
  padding: 40px;
}

.search-table {
  width: 100%;
  margin-bottom: 20px;
}

.search-table :deep(.el-table__row) {
  cursor: pointer;
}

.search-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.order-number {
  font-weight: 600;
  color: #409eff;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>