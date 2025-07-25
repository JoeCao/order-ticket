import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { orderApi, type Order, type OrderSearchParams, type OrderStatistics, type PageResponse } from '@/api/orderApi'

export const useOrderStore = defineStore('order', () => {
  // State
  const orders = ref<Order[]>([])
  const currentOrder = ref<Order | null>(null)
  const statistics = ref<OrderStatistics | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  
  // Pagination state
  const currentPage = ref(0)
  const pageSize = ref(10)
  const totalElements = ref(0)
  const totalPages = ref(0)
  
  // Search filters
  const searchParams = ref<OrderSearchParams>({
    orderNumber: '',
    customerName: '',
    status: '',
    startDate: '',
    endDate: '',
  })

  // Getters
  const hasOrders = computed(() => orders.value.length > 0)
  const hasError = computed(() => error.value !== null)
  const isLoading = computed(() => loading.value)
  
  // Actions
  const setLoading = (state: boolean) => {
    loading.value = state
  }

  const setError = (message: string | null) => {
    error.value = message
  }

  const clearError = () => {
    error.value = null
  }

  const setOrders = (pageResponse: PageResponse<Order>) => {
    orders.value = pageResponse.content
    totalElements.value = pageResponse.totalElements
    totalPages.value = pageResponse.totalPages
    currentPage.value = pageResponse.number
    pageSize.value = pageResponse.size
  }

  const setCurrentOrder = (order: Order | null) => {
    currentOrder.value = order
  }

  const setStatistics = (stats: OrderStatistics) => {
    statistics.value = stats
  }

  const updateSearchParams = (params: Partial<OrderSearchParams>) => {
    searchParams.value = { ...searchParams.value, ...params }
  }

  const clearSearchParams = () => {
    searchParams.value = {
      orderNumber: '',
      customerName: '',
      status: '',
      startDate: '',
      endDate: '',
    }
  }

  // API calls
  const fetchOrders = async (page = 0, size = 10) => {
    setLoading(true)
    clearError()
    try {
      const response = await orderApi.getOrders(page, size)
      setOrders(response)
    } catch (err) {
      setError('Failed to fetch orders')
      console.error('Error fetching orders:', err)
    } finally {
      setLoading(false)
    }
  }

  const searchOrders = async (params?: OrderSearchParams) => {
    setLoading(true)
    clearError()
    try {
      const searchParams = params || {
        ...searchParams.value,
        page: currentPage.value,
        size: pageSize.value,
      }
      const response = await orderApi.searchOrders(searchParams)
      setOrders(response)
    } catch (err) {
      setError('Failed to search orders')
      console.error('Error searching orders:', err)
    } finally {
      setLoading(false)
    }
  }

  const fetchOrderById = async (id: number) => {
    setLoading(true)
    clearError()
    try {
      const order = await orderApi.getOrderById(id)
      setCurrentOrder(order)
      return order
    } catch (err) {
      setError('Failed to fetch order details')
      console.error('Error fetching order:', err)
      return null
    } finally {
      setLoading(false)
    }
  }

  const fetchOrderByNumber = async (orderNumber: string) => {
    setLoading(true)
    clearError()
    try {
      const order = await orderApi.getOrderByNumber(orderNumber)
      setCurrentOrder(order)
      return order
    } catch (err) {
      setError('Order not found')
      console.error('Error fetching order by number:', err)
      return null
    } finally {
      setLoading(false)
    }
  }

  const fetchStatistics = async () => {
    try {
      const stats = await orderApi.getOrderStatistics()
      setStatistics(stats)
    } catch (err) {
      console.error('Error fetching statistics:', err)
    }
  }

  const createOrder = async (orderData: Omit<Order, 'id'>) => {
    setLoading(true)
    clearError()
    try {
      const newOrder = await orderApi.createOrder(orderData)
      // Refresh orders list
      await fetchOrders(currentPage.value, pageSize.value)
      return newOrder
    } catch (err) {
      setError('Failed to create order')
      console.error('Error creating order:', err)
      return null
    } finally {
      setLoading(false)
    }
  }

  const updateOrder = async (id: number, orderData: Order) => {
    setLoading(true)
    clearError()
    try {
      const updatedOrder = await orderApi.updateOrder(id, orderData)
      // Refresh orders list
      await fetchOrders(currentPage.value, pageSize.value)
      return updatedOrder
    } catch (err) {
      setError('Failed to update order')
      console.error('Error updating order:', err)
      return null
    } finally {
      setLoading(false)
    }
  }

  const deleteOrder = async (id: number) => {
    setLoading(true)
    clearError()
    try {
      await orderApi.deleteOrder(id)
      // Refresh orders list
      await fetchOrders(currentPage.value, pageSize.value)
      return true
    } catch (err) {
      setError('Failed to delete order')
      console.error('Error deleting order:', err)
      return false
    } finally {
      setLoading(false)
    }
  }

  const changePage = async (page: number) => {
    currentPage.value = page
    if (Object.values(searchParams.value).some(v => v)) {
      await searchOrders()
    } else {
      await fetchOrders(page, pageSize.value)
    }
  }

  const changePageSize = async (size: number) => {
    pageSize.value = size
    currentPage.value = 0
    if (Object.values(searchParams.value).some(v => v)) {
      await searchOrders()
    } else {
      await fetchOrders(0, size)
    }
  }

  return {
    // State
    orders,
    currentOrder,
    statistics,
    loading,
    error,
    currentPage,
    pageSize,
    totalElements,
    totalPages,
    searchParams,
    
    // Getters
    hasOrders,
    hasError,
    isLoading,
    
    // Actions
    setLoading,
    setError,
    clearError,
    setCurrentOrder,
    updateSearchParams,
    clearSearchParams,
    fetchOrders,
    searchOrders,
    fetchOrderById,
    fetchOrderByNumber,
    fetchStatistics,
    createOrder,
    updateOrder,
    deleteOrder,
    changePage,
    changePageSize,
  }
})