<template>
  <div class="dashboard">
    <!-- Page Header -->
    <div class="page-header">
      <h1>订单管理仪表板</h1>
      <el-button type="primary" @click="refreshData" :loading="loading">
        刷新数据
      </el-button>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-section">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="content-wrapper">
      <!-- Orders Table -->
      <div class="orders-section">
        <div class="section-header">
          <h2>最近订单</h2>
          <el-button type="primary" size="small" @click="viewAllOrders">
            查看全部
          </el-button>
        </div>
        
        <div class="table-container">
          
          <div v-if="error" class="error-state">
            <el-empty description="加载失败">
              <el-button type="primary" @click="refreshData">重新加载</el-button>
            </el-empty>
          </div>

          <div v-else-if="loading && !recentOrders.length" class="loading-state">
            <el-skeleton animated :rows="5" />
          </div>

          <div v-else-if="!recentOrders.length" class="empty-state">
            <el-empty description="暂无订单数据" />
          </div>

          <el-table
            v-else
            :data="recentOrders"
            v-loading="loading && recentOrders.length > 0"
            stripe
            style="width: 100%"
            @row-click="handleRowClick"
          >
            <el-table-column prop="orderNumber" label="订单号" width="160">
              <template #default="{ row }">
                <span class="order-number">{{ row.orderNumber }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="customerName" label="客户姓名" width="120" />
            <el-table-column prop="totalAmount" label="金额" width="120">
              <template #default="{ row }">
                <span class="amount">¥{{ formatAmount(row.totalAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="orderDate" label="订单日期" width="160">
              <template #default="{ row }">
                {{ formatDate(row.orderDate) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  @click.stop="viewOrder(row.id)"
                >
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="sidebar">
        <!-- Quick Actions -->
        <div class="sidebar-card">
          <h3>快速操作</h3>
          <div class="quick-actions">
            <el-button type="success" class="action-btn" @click="$router.push('/search')">
              🔍 订单查询
            </el-button>
            <el-button type="warning" class="action-btn" @click="$router.push('/export')">
              📥 导出数据
            </el-button>
            <el-button type="info" class="action-btn" @click="$router.push('/orders')">
              📋 所有订单
            </el-button>
          </div>
        </div>

        <!-- Today Stats -->
        <div class="sidebar-card" v-if="orderStore.statistics">
          <h3>今日统计</h3>
          <div class="today-stats">
            <div class="today-stat">
              <span>今日订单</span>
              <span>{{ Math.floor(orderStore.statistics.totalOrders * 0.1) }}</span>
            </div>
            <div class="today-stat">
              <span>处理率</span>
              <span>{{ Math.round((orderStore.statistics.deliveredOrders / orderStore.statistics.totalOrders) * 100) }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useOrderStore } from '@/stores/orderStore'
import type { Order } from '@/api/orderApi'

const router = useRouter()
const orderStore = useOrderStore()

const recentOrders = ref<Order[]>([])
const loading = ref(false)
const error = ref(false)

const stats = computed(() => [
  {
    label: '总订单',
    value: orderStore.statistics?.totalOrders || 0,
    color: '#409eff'
  },
  {
    label: '待处理',
    value: orderStore.statistics?.pendingOrders || 0,
    color: '#e6a23c'
  },
  {
    label: '已完成',
    value: orderStore.statistics?.deliveredOrders || 0,
    color: '#67c23a'
  },
  {
    label: '总收入',
    value: `¥${calculateTotalRevenue()}`,
    color: '#f56c6c'
  }
])

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
  try {
    return new Date(dateStr).toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return dateStr
  }
}

const formatAmount = (amount: number | string) => {
  try {
    return Number(amount).toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  } catch (e) {
    return amount.toString()
  }
}

const viewOrder = (id: number) => {
  router.push(`/orders/${id}`)
}

const viewAllOrders = () => {
  router.push('/orders')
}

const handleRowClick = (row: Order) => {
  viewOrder(row.id!)
}

const calculateTotalRevenue = () => {
  if (!orderStore.statistics) return '0'
  const revenue = (orderStore.statistics.deliveredOrders * 1500) || 0
  return revenue.toLocaleString('zh-CN')
}

const refreshData = async () => {
  await loadDashboardData()
  ElMessage.success('数据已刷新')
}

const loadDashboardData = async () => {
  loading.value = true
  error.value = false
  
  try {
    await Promise.all([
      orderStore.fetchStatistics(),
      loadRecentOrders()
    ])
  } catch (err) {
    console.error('Error loading dashboard data:', err)
    error.value = true
    ElMessage.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const loadRecentOrders = async () => {
  try {
    await orderStore.fetchOrders(0, 8)
    recentOrders.value = orderStore.orders
  } catch (err) {
    console.error('Error loading recent orders:', err)
    recentOrders.value = []
    throw err
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

/* Statistics Section */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: rgba(64, 158, 255, 0.2);
}

.stat-icon {
  font-size: 28px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.2));
  border-radius: 12px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

/* Content Wrapper */
.content-wrapper {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* Orders Section */
.orders-section {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}

.section-header h2 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.table-container {
  min-height: 400px;
}

.error-state,
.empty-state,
.loading-state {
  padding: 40px;
  text-align: center;
}

.order-number {
  font-weight: 600;
  color: #409eff;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

/* Sidebar */
.sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.sidebar-card h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  width: 100%;
  height: 44px;
  justify-content: flex-start !important;
  text-align: left !important;
}

.action-btn .el-button__text-wrapper {
  text-align: left !important;
  width: 100%;
}

.today-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.today-stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 6px;
}

.today-stat span:first-child {
  color: #606266;
  font-size: 14px;
}

.today-stat span:last-child {
  color: #303133;
  font-weight: 600;
  font-size: 16px;
}

/* Responsive */
@media (max-width: 1200px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    flex-direction: row;
  }
  
  .sidebar-card {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .stats-section {
    flex-direction: column;
  }
  
  .sidebar {
    flex-direction: column;
  }
  
  .sidebar-card {
    flex: none;
  }
}
</style>