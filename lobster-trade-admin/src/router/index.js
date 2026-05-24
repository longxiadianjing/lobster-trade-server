import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { ElMessage } from 'element-plus'

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
      { path: 'users', name: 'Users', component: () => import('@/views/UserManage.vue'), meta: { permissions: ['USER_VIEW'] } },
      { path: 'orders', name: 'Orders', component: () => import('@/views/OrderManage.vue'), meta: { permissions: ['ORDER_MANAGE'] } },
      { path: 'products', name: 'Products', component: () => import('@/views/ProductManage.vue'), meta: { permissions: ['PRODUCT_MANAGE'] } },
      { path: 'disputes', name: 'Disputes', component: () => import('@/views/DisputeManage.vue'), meta: { permissions: ['ORDER_MANAGE'] } },
      { path: 'games', name: 'Games', component: () => import('@/views/GameManage.vue'), meta: { permissions: ['GAME_MANAGE'] } },
      { path: 'order-input', name: 'OrderInput', component: () => import('@/views/OrderInput.vue'), meta: { permissions: ['ORDER_MANAGE'] } },
      { path: 'certifications', name: 'Certifications', component: () => import('@/views/CertificationManage.vue'), meta: { permissions: ['USER_VIEW'] } },
      { path: 'hot-search', name: 'HotSearch', component: () => import('@/views/HotSearchManage.vue'), meta: { permissions: ['CONFIG_MANAGE'] } },
      { path: 'recommend-slot', name: 'RecommendSlot', component: () => import('@/views/ProductRecommendSlotManage.vue'), meta: { permissions: ['CONFIG_MANAGE'] } },
      { path: 'announcements', name: 'Announcements', component: () => import('@/views/AnnouncementManage.vue'), meta: { permissions: ['CONFIG_MANAGE'] } },
      { path: 'notifications', name: 'Notifications', component: () => import('@/views/NotificationManage.vue'), meta: { permissions: ['CONFIG_MANAGE'] } },
      { path: 'tickets', name: 'Tickets', component: () => import('@/views/TicketManage.vue'), meta: { permissions: ['TICKET_MANAGE'] } },
      { path: 'customer-service', name: 'CustomerService', component: () => import('@/views/CustomerServiceManage.vue'), meta: { permissions: ['TICKET_MANAGE'] } },
      { path: 'reviews', name: 'Reviews', component: () => import('@/views/ReviewManage.vue'), meta: { permissions: ['REVIEW_MANAGE'] } },
      { path: 'export', name: 'Export', component: () => import('@/views/ExportManage.vue'), meta: { permissions: ['ORDER_MANAGE'] } },
      { path: 'real-name', name: 'RealName', component: () => import('@/views/RealNameManage.vue'), meta: { permissions: ['USER_VIEW'] } },
      { path: 'audit-log', name: 'AuditLog', component: () => import('@/views/AuditLogManage.vue'), meta: { permissions: ['AUDIT_LOG'] } },
      { path: 'coupon', name: 'Coupon', component: () => import('@/views/CouponManage.vue'), meta: { permissions: ['COUPON_MANAGE'] } },
      { path: 'payment-config', name: 'PaymentConfig', component: () => import('@/views/PaymentConfig.vue'), meta: { permissions: ['CONFIG_VIEW'] } },
      { path: 'sms-config', name: 'SmsConfig', component: () => import('@/views/SmsConfig.vue'), meta: { permissions: ['CONFIG_VIEW'] } },
      { path: 'admin-manage', name: 'AdminManage', component: () => import('@/views/AdminManage.vue'), meta: { permissions: ['ADMIN_MANAGE'] } },
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
    const requiredPerms = to.meta.permissions
    if (requiredPerms && requiredPerms.length > 0) {
      // 超级管理员 * 拥有所有权限
      if (adminStore.permissions === '*') {
        next()
        return
      }
      // 普通管理员必须有所有必需权限
      if (Array.isArray(adminStore.permissions) && !requiredPerms.every(p => adminStore.permissions.includes(p))) {
        ElMessage.warning('权限不足，无法访问该页面')
        return
      }
    }
    next()
  }
})

export default router