<template>
  <div class="order-list">
    <div class="page-header">
      <h2>订单列表</h2>
      <el-button type="primary" @click="refreshOrders">
        <el-icon><refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-card class="filter-card">
      <div class="filter-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchParams.orderNumber"
              placeholder="订单号"
              clearable
              @keyup.enter="searchOrders"
            >
              <template #prefix>
                <el-icon><document /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchParams.customerName"
              placeholder="客户姓名"
              clearable
              @keyup.enter="searchOrders"
            >
              <template #prefix>
                <el-icon><user /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchParams.status"
              placeholder="订单状态"
              clearable
            >
              <el-option label="待处理" value="PENDING" />
              <el-option label="已确认" value="CONFIRMED" />
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="已发货" value="SHIPPED" />
              <el-option label="已送达" value="DELIVERED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <div class="filter-actions">
              <el-button type="primary" @click="searchOrders">
                <el-icon><search /></el-icon>
                搜索
              </el-button>
              <el-button @click="clearFilters">
                <el-icon><refresh /></el-icon>
                重置
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table
        :data="orderStore.orders"
        v-loading="orderStore.loading"
        style="width: 100%"
        @row-click="viewOrderDetail"
        class="order-table"
      >
        <el-table-column prop="orderNumber" label="订单号" width="150" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="customerEmail" label="客户邮箱" width="200" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderDate" label="订单日期" width="180">
          <template #default="{ row }">
            {{ formatDate(row.orderDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click.stop="viewOrderDetail(row)">
              查看详情
            </el-button>
            <el-button type="success" size="small" @click.stop="exportInvoice(row.id)">
              导出发票
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="orderStore.totalElements"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Error message -->
    <el-alert
      v-if="orderStore.hasError"
      :title="orderStore.error"
      type="error"
      :closable="false"
      style="margin-top: 20px"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/orderStore'
import { exportApi, downloadBlob } from '@/api/orderApi'
import { ElMessage } from 'element-plus'

const router = useRouter()
const orderStore = useOrderStore()

const currentPage = ref(1)
const pageSize = ref(10)

const searchParams = reactive({
  orderNumber: '',
  customerName: '',
  status: '',
})

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

const viewOrderDetail = (row: any) => {
  router.push(`/orders/${row.id}`)
}

const exportInvoice = async (orderId: number) => {
  try {
    const blob = await exportApi.exportSingleInvoicePdf(orderId)
    downloadBlob(blob, `invoice_${orderId}.pdf`)
    ElMessage.success('发票导出成功')
  } catch (error) {
    console.error('Export failed:', error)
    ElMessage.error('发票导出失败')
  }
}

const searchOrders = async () => {
  currentPage.value = 1
  await orderStore.searchOrders({
    ...searchParams,
    page: 0,
    size: pageSize.value
  })
}

const clearFilters = async () => {
  searchParams.orderNumber = ''
  searchParams.customerName = ''
  searchParams.status = ''
  currentPage.value = 1
  await orderStore.fetchOrders(0, pageSize.value)
}

const refreshOrders = async () => {
  if (hasSearchParams()) {
    await searchOrders()
  } else {
    await orderStore.fetchOrders(currentPage.value - 1, pageSize.value)
  }
}

const hasSearchParams = () => {
  return searchParams.orderNumber || searchParams.customerName || searchParams.status
}

const handleSizeChange = async (val: number) => {
  pageSize.value = val
  await orderStore.changePageSize(val)
}

const handleCurrentChange = async (val: number) => {
  currentPage.value = val
  if (hasSearchParams()) {
    await orderStore.searchOrders({
      ...searchParams,
      page: val - 1,
      size: pageSize.value
    })
  } else {
    await orderStore.changePage(val - 1)
  }
}

onMounted(async () => {
  await orderStore.fetchOrders(0, pageSize.value)
})
</script>

<style scoped>
.order-list {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  padding: 10px 0;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.order-table {
  margin-bottom: 20px;
}

.order-table :deep(.el-table__row) {
  cursor: pointer;
}

.order-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.amount {
  font-weight: bold;
  color: #f56c6c;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>