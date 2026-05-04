import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'users', name: 'Users', component: () => import('@/views/UserManage.vue') },
      { path: 'orders', name: 'Orders', component: () => import('@/views/OrderManage.vue') },
      { path: 'products', name: 'Products', component: () => import('@/views/ProductManage.vue') },
      { path: 'disputes', name: 'Disputes', component: () => import('@/views/DisputeManage.vue') },
      { path: 'games', name: 'Games', component: () => import('@/views/GameManage.vue') },
      { path: 'order-input', name: 'OrderInput', component: () => import('@/views/OrderInput.vue') },
      { path: 'certifications', name: 'Certifications', component: () => import('@/views/CertificationManage.vue') },
      { path: 'hot-search', name: 'HotSearch', component: () => import('@/views/HotSearchManage.vue') },
      { path: 'recommend-slot', name: 'RecommendSlot', component: () => import('@/views/ProductRecommendSlotManage.vue') },
      { path: 'announcements', name: 'Announcements', component: () => import('@/views/AnnouncementManage.vue') },
      { path: 'notifications', name: 'Notifications', component: () => import('@/views/NotificationManage.vue') },
      { path: 'tickets', name: 'Tickets', component: () => import('@/views/TicketManage.vue') },
      { path: 'customer-service', name: 'CustomerService', component: () => import('@/views/CustomerServiceManage.vue') },
      { path: 'reviews', name: 'Reviews', component: () => import('@/views/ReviewManage.vue') },
      { path: 'export', name: 'Export', component: () => import('@/views/ExportManage.vue') },
      { path: 'real-name', name: 'RealName', component: () => import('@/views/RealNameManage.vue') },
      { path: 'audit-log', name: 'AuditLog', component: () => import('@/views/AuditLogManage.vue') },
      { path: 'coupon', name: 'Coupon', component: () => import('@/views/CouponManage.vue') },
      { path: 'admin-manage', name: 'AdminManage', component: () => import('@/views/AdminManage.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory('/admin'),
  routes
})

router.beforeEach((to, from, next) => {
  const adminStore = useAdminStore()
  if (to.meta.requiresAuth && !adminStore.token) {
    next('/login')
  } else if (to.path === '/login' && adminStore.token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router