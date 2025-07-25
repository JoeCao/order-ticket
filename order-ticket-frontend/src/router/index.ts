import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('../views/OrderListView.vue'),
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('../views/OrderDetailView.vue'),
      props: true,
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('../views/OrderSearchView.vue'),
    },
    {
      path: '/export',
      name: 'export',
      component: () => import('../views/ExportView.vue'),
    },
  ],
})

export default router
