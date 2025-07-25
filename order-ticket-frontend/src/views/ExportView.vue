<template>
  <div class="export-view">
    <div class="page-header">
      <h2>数据导出</h2>
      <el-button type="primary" @click="showExportPreview" :disabled="!hasValidFilters">
        <el-icon><view /></el-icon>
        预览导出数据
      </el-button>
    </div>

    <el-row :gutter="20">
      <!-- Export Configuration -->
      <el-col :span="8">
        <el-card class="config-card">
          <template #header>
            <div class="card-header">
              <el-icon><setting /></el-icon>
              <span>导出配置</span>
            </div>
          </template>

          <el-form :model="exportConfig" label-width="100px" class="export-form">
            <el-form-item label="导出格式">
              <el-radio-group v-model="exportConfig.format">
                <el-radio value="excel">
                  <el-icon><document /></el-icon>
                  Excel (.xlsx)
                </el-radio>
                <el-radio value="csv">
                  <el-icon><document /></el-icon>
                  CSV (.csv)
                </el-radio>
                <el-radio value="pdf">
                  <el-icon><document /></el-icon>
                  PDF 发票
                </el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="文件名">
              <el-input
                v-model="exportConfig.filename"
                placeholder="请输入文件名"
                maxlength="50"
                show-word-limit
              >
                <template #suffix>
                  <span class="file-extension">{{ getFileExtension() }}</span>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="导出字段" v-if="exportConfig.format !== 'pdf'">
              <el-checkbox-group v-model="exportConfig.fields">
                <div class="field-checkboxes">
                  <el-checkbox value="orderNumber">订单号</el-checkbox>
                  <el-checkbox value="customerName">客户姓名</el-checkbox>
                  <el-checkbox value="customerEmail">客户邮箱</el-checkbox>
                  <el-checkbox value="customerPhone">客户电话</el-checkbox>
                  <el-checkbox value="totalAmount">订单金额</el-checkbox>
                  <el-checkbox value="status">订单状态</el-checkbox>
                  <el-checkbox value="orderDate">订单日期</el-checkbox>
                  <el-checkbox value="createdAt">创建时间</el-checkbox>
                  <el-checkbox value="description">订单描述</el-checkbox>
                  <el-checkbox value="productDetails">产品详情</el-checkbox>
                </div>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item>
              <el-button 
                type="success" 
                @click="performExport" 
                :loading="exporting"
                :disabled="!hasValidFilters || exportConfig.fields.length === 0"
                style="width: 100%"
              >
                <el-icon><download /></el-icon>
                {{ exporting ? '导出中...' : '开始导出' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- Export History -->
        <el-card class="history-card" v-if="exportHistory.length > 0">
          <template #header>
            <div class="card-header">
              <el-icon><clock /></el-icon>
              <span>导出历史</span>
              <el-button size="small" text @click="clearHistory">
                清空历史
              </el-button>
            </div>
          </template>

          <div class="export-history">
            <div 
              v-for="(record, index) in exportHistory" 
              :key="index"
              class="history-item"
            >
              <div class="history-info">
                <div class="history-filename">{{ record.filename }}</div>
                <div class="history-meta">
                  {{ record.format.toUpperCase() }} · {{ record.count }} 条记录 · {{ formatDateTime(record.timestamp) }}
                </div>
              </div>
              <el-button 
                size="small" 
                type="primary" 
                text
                @click="reExport(record)"
              >
                重新导出
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Filter Configuration -->
      <el-col :span="16">
        <el-card class="filter-card">
          <template #header>
            <div class="card-header">
              <el-icon><filter /></el-icon>
              <span>筛选条件</span>
              <el-button size="small" @click="clearFilters">
                <el-icon><refresh /></el-icon>
                重置条件
              </el-button>
            </div>
          </template>

          <el-form :model="filterForm" label-width="120px" class="filter-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="订单号">
                  <el-input
                    v-model="filterForm.orderNumber"
                    placeholder="请输入订单号"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户姓名">
                  <el-input
                    v-model="filterForm.customerName"
                    placeholder="请输入客户姓名"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="订单状态">
                  <el-select
                    v-model="filterForm.status"
                    placeholder="请选择订单状态"
                    clearable
                    multiple
                    collapse-tags
                    style="width: 100%"
                  >
                    <el-option
                      v-for="status in statusOptions"
                      :key="status.value"
                      :label="status.label"
                      :value="status.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="订单日期">
                  <el-date-picker
                    v-model="filterForm.dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                    @change="handleDateRangeChange"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="订单金额范围">
                  <div class="amount-range">
                    <el-input-number
                      v-model="filterForm.minAmount"
                      placeholder="最小金额"
                      :min="0"
                      :precision="2"
                      controls-position="right"
                      style="width: 48%"
                    />
                    <span class="range-separator">-</span>
                    <el-input-number
                      v-model="filterForm.maxAmount"
                      placeholder="最大金额"
                      :min="0"
                      :precision="2"
                      controls-position="right"
                      style="width: 48%"
                    />
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="创建时间">
                  <el-date-picker
                    v-model="filterForm.createdDateRange"
                    type="datetimerange"
                    range-separator="至"
                    start-placeholder="创建开始时间"
                    end-placeholder="创建结束时间"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%"
                    @change="handleCreatedDateRangeChange"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row>
              <el-col :span="24">
                <div class="filter-summary" v-if="hasValidFilters">
                  <el-tag type="info">{{ getFilterSummary() }}</el-tag>
                </div>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <!-- Export Preview -->
        <el-card class="preview-card" v-if="previewData.length > 0">
          <template #header>
            <div class="card-header">
              <el-icon><view /></el-icon>
              <span>数据预览 ({{ previewData.length }} 条记录)</span>
              <el-button size="small" @click="refreshPreview">
                <el-icon><refresh /></el-icon>
                刷新预览
              </el-button>
            </div>
          </template>

          <el-table
            :data="previewData.slice(0, 10)"
            v-loading="previewLoading"
            style="width: 100%"
            max-height="400"
          >
            <el-table-column 
              v-for="field in exportConfig.fields" 
              :key="field"
              :prop="field"
              :label="getFieldLabel(field)"
              :width="getColumnWidth(field)"
            >
              <template #default="{ row }">
                <span v-if="field === 'totalAmount'" class="amount">¥{{ row[field] }}</span>
                <el-tag v-else-if="field === 'status'" :type="getStatusType(row[field])">
                  {{ getStatusLabel(row[field]) }}
                </el-tag>
                <span v-else-if="field === 'orderDate' || field === 'createdAt'">
                  {{ formatDateTime(row[field]) }}
                </span>
                <span v-else>{{ row[field] || '-' }}</span>
              </template>
            </el-table-column>
          </el-table>

          <div class="preview-footer" v-if="previewData.length > 10">
            <el-text type="info">显示前10条记录，总共{{ previewData.length }}条</el-text>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Export Progress Dialog -->
    <el-dialog
      v-model="exportProgressVisible"
      title="导出进度"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="export-progress">
        <el-progress
          :percentage="exportProgress"
          :status="exportProgress === 100 ? 'success' : undefined"
        />
        <p class="progress-text">{{ exportProgressText }}</p>
      </div>
      <template #footer v-if="exportProgress === 100">
        <el-button type="primary" @click="exportProgressVisible = false">
          完成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useOrderStore } from '@/stores/orderStore'
import { orderApi, exportApi, downloadBlob, type OrderSearchParams, type Order } from '@/api/orderApi'
import { ElMessage, ElMessageBox } from 'element-plus'

const orderStore = useOrderStore()

const exporting = ref(false)
const previewLoading = ref(false)
const previewData = ref<Order[]>([])

const exportProgressVisible = ref(false)
const exportProgress = ref(0)
const exportProgressText = ref('')

const exportConfig = reactive({
  format: 'excel' as 'excel' | 'csv' | 'pdf',
  filename: '',
  fields: ['orderNumber', 'customerName', 'customerEmail', 'totalAmount', 'status', 'orderDate'] as string[]
})

const filterForm = reactive({
  orderNumber: '',
  customerName: '',
  status: [] as string[],
  dateRange: null as [string, string] | null,
  startDate: '',
  endDate: '',
  createdDateRange: null as [string, string] | null,
  createdStartDate: '',
  createdEndDate: '',
  minAmount: null as number | null,
  maxAmount: null as number | null,
})

const exportHistory = ref<Array<{
  filename: string
  format: string
  count: number
  timestamp: string
  filters: any
}>>([])

const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已送达', value: 'DELIVERED' },
  { label: '已取消', value: 'CANCELLED' }
]

const fieldLabels = {
  orderNumber: '订单号',
  customerName: '客户姓名',
  customerEmail: '客户邮箱',
  customerPhone: '客户电话',
  totalAmount: '订单金额',
  status: '订单状态',
  orderDate: '订单日期',
  createdAt: '创建时间',
  description: '订单描述',
  productDetails: '产品详情'
}

const hasValidFilters = computed(() => {
  return filterForm.orderNumber ||
         filterForm.customerName ||
         filterForm.status.length > 0 ||
         filterForm.startDate ||
         filterForm.endDate ||
         filterForm.minAmount !== null ||
         filterForm.maxAmount !== null ||
         filterForm.createdStartDate ||
         filterForm.createdEndDate
})

const getFileExtension = () => {
  const extensions = {
    excel: '.xlsx',
    csv: '.csv',
    pdf: '.pdf'
  }
  return extensions[exportConfig.format]
}

const getFieldLabel = (field: string) => {
  return fieldLabels[field as keyof typeof fieldLabels] || field
}

const getColumnWidth = (field: string) => {
  const widths: Record<string, number> = {
    orderNumber: 150,
    customerName: 120,
    customerEmail: 200,
    customerPhone: 130,
    totalAmount: 120,
    status: 100,
    orderDate: 150,
    createdAt: 180,
    description: 200,
    productDetails: 200
  }
  return widths[field] || 150
}

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

const formatDateTime = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const handleDateRangeChange = (dates: [string, string] | null) => {
  if (dates) {
    filterForm.startDate = dates[0]
    filterForm.endDate = dates[1]
  } else {
    filterForm.startDate = ''
    filterForm.endDate = ''
  }
}

const handleCreatedDateRangeChange = (dates: [string, string] | null) => {
  if (dates) {
    filterForm.createdStartDate = dates[0]
    filterForm.createdEndDate = dates[1]
  } else {
    filterForm.createdStartDate = ''
    filterForm.createdEndDate = ''
  }
}

const buildSearchParams = (): OrderSearchParams => {
  const params: OrderSearchParams = {}

  if (filterForm.orderNumber) params.orderNumber = filterForm.orderNumber
  if (filterForm.customerName) params.customerName = filterForm.customerName
  if (filterForm.status.length > 0) params.status = filterForm.status.join(',')
  if (filterForm.startDate) params.startDate = filterForm.startDate
  if (filterForm.endDate) params.endDate = filterForm.endDate

  return params
}

const getFilterSummary = () => {
  const conditions = []
  if (filterForm.orderNumber) conditions.push(`订单号: ${filterForm.orderNumber}`)
  if (filterForm.customerName) conditions.push(`客户: ${filterForm.customerName}`)
  if (filterForm.status.length > 0) conditions.push(`状态: ${filterForm.status.length}项`)
  if (filterForm.startDate || filterForm.endDate) conditions.push('日期范围已设置')
  if (filterForm.minAmount !== null || filterForm.maxAmount !== null) conditions.push('金额范围已设置')
  
  return conditions.join(' | ')
}

const clearFilters = () => {
  filterForm.orderNumber = ''
  filterForm.customerName = ''
  filterForm.status = []
  filterForm.dateRange = null
  filterForm.startDate = ''
  filterForm.endDate = ''
  filterForm.createdDateRange = null
  filterForm.createdStartDate = ''
  filterForm.createdEndDate = ''
  filterForm.minAmount = null
  filterForm.maxAmount = null
  previewData.value = []
}

const showExportPreview = async () => {
  if (!hasValidFilters.value) {
    ElMessage.warning('请设置筛选条件')
    return
  }

  previewLoading.value = true
  try {
    const params = buildSearchParams()
    const orders = await orderApi.getExportPreview(params)
    previewData.value = orders
    ElMessage.success(`预览成功，共 ${orders.length} 条记录`)
  } catch (error) {
    console.error('Preview failed:', error)
    ElMessage.error('预览失败')
  } finally {
    previewLoading.value = false
  }
}

const refreshPreview = () => {
  showExportPreview()
}

const performExport = async () => {
  if (!hasValidFilters.value) {
    ElMessage.warning('请设置筛选条件')
    return
  }

  if (exportConfig.fields.length === 0 && exportConfig.format !== 'pdf') {
    ElMessage.warning('请选择要导出的字段')
    return
  }

  if (!exportConfig.filename.trim()) {
    exportConfig.filename = `export_${new Date().toISOString().split('T')[0]}`
  }

  exporting.value = true
  exportProgressVisible.value = true
  exportProgress.value = 0
  exportProgressText.value = '准备导出...'

  try {
    const params = buildSearchParams()
    
    // Simulate progress
    const progressInterval = setInterval(() => {
      if (exportProgress.value < 90) {
        exportProgress.value += Math.random() * 10
        exportProgressText.value = '正在处理数据...'
      }
    }, 100)

    let blob: Blob
    const filename = `${exportConfig.filename}${getFileExtension()}`

    switch (exportConfig.format) {
      case 'excel':
        exportProgressText.value = '生成Excel文件...'
        blob = await exportApi.exportToExcel(params)
        break
      case 'pdf':
        exportProgressText.value = '生成PDF文件...'
        blob = await exportApi.exportBatchInvoicesPdf(params)
        break
      case 'csv':
        // Assuming CSV export is available
        exportProgressText.value = '生成CSV文件...'
        blob = await exportApi.exportToExcel(params) // Fallback to Excel for now
        break
      default:
        throw new Error('Unsupported export format')
    }

    clearInterval(progressInterval)
    exportProgress.value = 100
    exportProgressText.value = '导出完成!'

    downloadBlob(blob, filename)

    // Add to export history
    const historyRecord = {
      filename,
      format: exportConfig.format,
      count: previewData.value.length,
      timestamp: new Date().toISOString(),
      filters: { ...filterForm }
    }
    exportHistory.value.unshift(historyRecord)
    if (exportHistory.value.length > 10) {
      exportHistory.value = exportHistory.value.slice(0, 10)
    }
    localStorage.setItem('exportHistory', JSON.stringify(exportHistory.value))

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('Export failed:', error)
    ElMessage.error('导出失败')
    exportProgressVisible.value = false
  } finally {
    exporting.value = false
  }
}

const reExport = async (record: any) => {
  Object.assign(filterForm, record.filters)
  exportConfig.filename = record.filename.replace(getFileExtension(), '')
  await showExportPreview()
  ElMessage.info('已恢复历史导出配置')
}

const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空导出历史吗?', '确认清空', {
      type: 'warning'
    })
    exportHistory.value = []
    localStorage.removeItem('exportHistory')
    ElMessage.success('清空成功')
  } catch {
    // User cancelled
  }
}

onMounted(() => {
  // Set default filename
  exportConfig.filename = `orders_export_${new Date().toISOString().split('T')[0]}`

  // Load export history
  const history = localStorage.getItem('exportHistory')
  if (history) {
    try {
      exportHistory.value = JSON.parse(history)
    } catch (error) {
      console.error('Error loading export history:', error)
    }
  }
})
</script>

<style scoped>
.export-view {
  max-width: 1400px;
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

.config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.export-form {
  padding: 10px 0;
}

.field-checkboxes {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-extension {
  color: #909399;
  font-size: 12px;
}

.history-card {
  margin-bottom: 20px;
}

.export-history {
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.history-item:last-child {
  border-bottom: none;
}

.history-info {
  flex: 1;
}

.history-filename {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.history-meta {
  font-size: 12px;
  color: #909399;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  padding: 10px 0;
}

.amount-range {
  display: flex;
  align-items: center;
  gap: 10px;
}

.range-separator {
  color: #909399;
  font-weight: bold;
}

.filter-summary {
  text-align: center;
  margin-top: 10px;
}

.preview-card {
  margin-bottom: 20px;
}

.amount {
  font-weight: bold;
  color: #f56c6c;
}

.preview-footer {
  text-align: center;
  padding: 10px 0;
  background-color: #fafafa;
  margin-top: 10px;
}

.export-progress {
  text-align: center;
  padding: 20px 0;
}

.progress-text {
  margin-top: 10px;
  color: #606266;
}
</style>