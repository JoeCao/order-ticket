import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface Order {
  id?: number
  orderNumber: string
  customerName: string
  customerEmail?: string
  customerPhone?: string
  totalAmount: number
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'
  orderDate: string
  description?: string
  productDetails?: string
  createdAt?: string
  updatedAt?: string
}

export interface OrderSearchParams {
  orderNumber?: string
  customerName?: string
  status?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface OrderStatistics {
  totalOrders: number
  pendingOrders: number
  processingOrders: number
  deliveredOrders: number
  cancelledOrders: number
}

export const orderApi = {
  // Get all orders with pagination
  getOrders: (page = 0, size = 10): Promise<PageResponse<Order>> =>
    apiClient.get(`/orders?page=${page}&size=${size}`).then(res => res.data),

  // Search orders with filters
  searchOrders: (params: OrderSearchParams): Promise<PageResponse<Order>> =>
    apiClient.get('/orders/search', { params }).then(res => res.data),

  // Get order by ID
  getOrderById: (id: number): Promise<Order> =>
    apiClient.get(`/orders/${id}`).then(res => res.data),

  // Get order by order number
  getOrderByNumber: (orderNumber: string): Promise<Order> =>
    apiClient.get(`/orders/number/${orderNumber}`).then(res => res.data),

  // Get orders by customer name
  getOrdersByCustomer: (customerName: string, page = 0, size = 10): Promise<PageResponse<Order>> =>
    apiClient.get(`/orders/customer?customerName=${customerName}&page=${page}&size=${size}`).then(res => res.data),

  // Get orders by status
  getOrdersByStatus: (status: string, page = 0, size = 10): Promise<PageResponse<Order>> =>
    apiClient.get(`/orders/status/${status}?page=${page}&size=${size}`).then(res => res.data),

  // Get recent orders
  getRecentOrders: (page = 0, size = 10): Promise<PageResponse<Order>> =>
    apiClient.get(`/orders/recent?page=${page}&size=${size}`).then(res => res.data),

  // Get order statistics
  getOrderStatistics: (): Promise<OrderStatistics> =>
    apiClient.get('/orders/statistics').then(res => res.data),

  // Get orders for export preview
  getExportPreview: (params: OrderSearchParams): Promise<Order[]> =>
    apiClient.get('/export/preview', { params }).then(res => res.data),

  // Create new order
  createOrder: (order: Omit<Order, 'id'>): Promise<Order> =>
    apiClient.post('/orders', order).then(res => res.data),

  // Update existing order
  updateOrder: (id: number, order: Order): Promise<Order> =>
    apiClient.put(`/orders/${id}`, order).then(res => res.data),

  // Delete order
  deleteOrder: (id: number): Promise<void> =>
    apiClient.delete(`/orders/${id}`).then(res => res.data),
}

export const exportApi = {
  // Export single order as PDF invoice
  exportSingleInvoicePdf: (orderId: number): Promise<Blob> =>
    apiClient.get(`/export/invoice/pdf/${orderId}`, { responseType: 'blob' }).then(res => res.data),

  // Export batch orders as PDF invoices
  exportBatchInvoicesPdf: (params: OrderSearchParams): Promise<Blob> =>
    apiClient.get('/export/invoice/pdf/batch', { params, responseType: 'blob' }).then(res => res.data),

  // Export orders to Excel
  exportToExcel: (params: OrderSearchParams): Promise<Blob> =>
    apiClient.get('/export/excel', { params, responseType: 'blob' }).then(res => res.data),
}

// Helper function to download blob as file
export const downloadBlob = (blob: Blob, filename: string) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}