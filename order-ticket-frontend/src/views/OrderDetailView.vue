<template>
  <div class="order-detail">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" type="info" plain>
          <el-icon><arrow-left /></el-icon>
          返回
        </el-button>
        <h2 v-if="order">订单详情 - {{ order.orderNumber }}</h2>
        <h2 v-else>订单详情</h2>
      </div>
      <div class="header-actions" v-if="order">
        <el-button type="success" @click="exportInvoice">
          <el-icon><download /></el-icon>
          导出发票
        </el-button>
      </div>
    </div>

    <div v-loading="orderStore.loading">
      <el-alert
        v-if="orderStore.hasError"
        :title="orderStore.error"
        type="error"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <div v-if="order" class="order-content">
        <!-- Basic Information -->
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-tag :type="getStatusType(order.status)" size="large">
                {{ getStatusLabel(order.status) }}
              </el-tag>
            </div>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">
              <el-text type="primary" size="large">{{ order.orderNumber }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(order.status)">
                {{ getStatusLabel(order.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <el-text type="danger" size="large" tag="b">¥{{ order.totalAmount }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="订单日期">
              {{ formatDate(order.orderDate) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDate(order.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDate(order.updatedAt) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- Customer Information -->
        <el-card class="info-card">
          <template #header>
            <span>客户信息</span>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="客户姓名">
              <el-text size="large">{{ order.customerName }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="客户邮箱">
              <el-link v-if="order.customerEmail" :href="'mailto:' + order.customerEmail">
                {{ order.customerEmail }}
              </el-link>
              <span v-else class="empty-text">未提供</span>
            </el-descriptions-item>
            <el-descriptions-item label="客户电话" :span="2">
              <el-link v-if="order.customerPhone" :href="'tel:' + order.customerPhone">
                {{ order.customerPhone }}
              </el-link>
              <span v-else class="empty-text">未提供</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- Order Details -->
        <el-card class="info-card">
          <template #header>
            <span>订单详情</span>
          </template>
          
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单描述">
              <div class="description-content">
                {{ order.description || '无描述' }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="产品详情">
              <div class="product-details">
                {{ order.productDetails || '无产品详情' }}
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- Status Timeline -->
        <el-card class="info-card">
          <template #header>
            <span>状态时间线</span>
          </template>
          
          <el-timeline>
            <el-timeline-item
              timestamp="订单创建"
              :value="formatDate(order.createdAt)"
              type="primary"
            >
              订单已创建，订单号：{{ order.orderNumber }}
            </el-timeline-item>
            
            <el-timeline-item
              v-if="order.status !== 'PENDING'"
              timestamp="订单确认"
              type="success"
            >
              订单已确认，进入处理流程
            </el-timeline-item>
            
            <el-timeline-item
              v-if="order.status === 'PROCESSING' || order.status === 'SHIPPED' || order.status === 'DELIVERED'"
              timestamp="订单处理"
              type="primary"
            >
              订单正在处理中
            </el-timeline-item>
            
            <el-timeline-item
              v-if="order.status === 'SHIPPED' || order.status === 'DELIVERED'"
              timestamp="订单发货"
              type="warning"
            >
              订单已发货，请注意查收
            </el-timeline-item>
            
            <el-timeline-item
              v-if="order.status === 'DELIVERED'"
              timestamp="订单送达"
              type="success"
            >
              订单已成功送达
            </el-timeline-item>
            
            <el-timeline-item
              v-if="order.status === 'CANCELLED'"
              timestamp="订单取消"
              type="danger"
            >
              订单已取消
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useOrderStore } from '@/stores/orderStore'
import { exportApi, downloadBlob, type Order } from '@/api/orderApi'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()

const order = ref<Order | null>(null)

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

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '未知'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const goBack = () => {
  router.back()
}

const exportInvoice = async () => {
  if (!order.value) return
  
  try {
    const blob = await exportApi.exportSingleInvoicePdf(order.value.id!)
    downloadBlob(blob, `invoice_${order.value.orderNumber}.pdf`)
    ElMessage.success('发票导出成功')
  } catch (error) {
    console.error('Export failed:', error)
    ElMessage.error('发票导出失败')
  }
}

const loadOrderDetail = async () => {
  const orderId = parseInt(route.params.id as string)
  if (!orderId || isNaN(orderId)) {
    ElMessage.error('无效的订单ID')
    router.push('/orders')
    return
  }

  const result = await orderStore.fetchOrderById(orderId)
  if (result) {
    order.value = result
  } else {
    ElMessage.error('订单不存在')
    router.push('/orders')
  }
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.order-detail {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-left h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-text {
  color: #909399;
  font-style: italic;
}

.description-content,
.product-details {
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.product-details {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-timeline-item__timestamp) {
  font-weight: 600;
  color: #909399;
}
</style>