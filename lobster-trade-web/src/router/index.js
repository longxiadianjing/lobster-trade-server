import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', guest: true }
  },
  {
    path: '/forget-password',
    name: 'ForgetPassword',
    component: () => import('@/views/auth/ForgetPassword.vue'),
    meta: { title: '找回密码', guest: true }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: '管理员登录', guest: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/index.vue'),
    meta: { title: '管理后台' }
  },
  {
    path: '/user/center',
    name: 'UserCenter',
    component: () => import('@/views/user/Center.vue'),
    meta: { title: '用户中心', requiresAuth: true }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/user/index.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/merchant',
    name: 'Merchant',
    redirect: '/order/center?role=seller',
    meta: { title: '商家中心', requiresAuth: true }
  },
  {
    path: '/fund',
    name: 'Fund',
    redirect: '/wallet',
    meta: { title: '资金中心', requiresAuth: true }
  },
  {
    path: '/user/real-name-verify',
    name: 'RealNameVerify',
    component: () => import('@/views/user/RealNameVerify.vue'),
    meta: { title: '实名认证', requiresAuth: true }
  },
  {
    path: '/user/set-pay-password',
    name: 'SetPayPassword',
    component: () => import('@/views/user/SetPayPassword.vue'),
    meta: { title: '设置支付密码', requiresAuth: true }
  },
  {
    path: '/user/security-center',
    name: 'SecurityCenter',
    component: () => import('@/views/user/SecurityCenter.vue'),
    meta: { title: '安全中心', requiresAuth: true }
  },
  {
    path: '/user/coupon-center',
    name: 'CouponCenter',
    component: () => import('@/views/user/CouponCenter.vue'),
    meta: { title: '优惠券中心', requiresAuth: true }
  },
  {
    path: '/user/message',
    name: 'UserMessage',
    component: () => import('@/views/user/Message.vue'),
    meta: { title: '消息中心', requiresAuth: true }
  },
  {
    path: '/user/favorites',
    name: 'UserFavorites',
    component: () => import('@/views/user/Favorites.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/wallet/index.vue'),
    meta: { title: '我的钱包', requiresAuth: true }
  },
  {
    path: '/wallet/recharge',
    name: 'Recharge',
    component: () => import('@/views/wallet/Recharge.vue'),
    meta: { title: '充值', requiresAuth: true }
  },
  {
    path: '/wallet/withdraw',
    name: 'Withdraw',
    component: () => import('@/views/wallet/Withdraw.vue'),
    meta: { title: '提现', requiresAuth: true }
  },
  {
    path: '/wallet/transactions',
    name: 'Transactions',
    component: () => import('@/views/wallet/Transactions.vue'),
    meta: { title: '交易流水', requiresAuth: true }
  },
  {
    path: '/product/list',
    name: 'ProductList',
    component: () => import('@/views/product/List.vue'),
    meta: { title: '商品列表' }
  },
  {
    path: '/product/detail/:id',
    name: 'ProductDetail',
    component: () => import('@/views/product/Detail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/seller/:id',
    name: 'SellerProfile',
    component: () => import('@/views/seller/SellerProfile.vue'),
    meta: { title: '服务商店铺' }
  },
  {
    path: '/product/publish',
    name: 'ProductPublish',
    component: () => import('@/views/product/Publish.vue'),
    meta: { title: '发布商品', requiresAuth: true }
  },
  {
    path: '/order/center',
    name: 'OrderCenter',
    component: () => import('@/views/order/Center.vue'),
    meta: { title: '订单中心', requiresAuth: true }
  },
  {
    path: '/order/detail/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/Detail.vue'),
    meta: { title: '订单详情', requiresAuth: true }
  },
  {
    path: '/order/deliver/:id',
    name: 'OrderDeliver',
    component: () => import('@/views/order/Deliver.vue'),
    meta: { title: '发货', requiresAuth: true }
  },
  {
    path: '/im',
    name: 'Im',
    component: () => import('@/views/im/index.vue'),
    meta: { title: '我的消息', requiresAuth: true }
  },
  {
    path: '/certification/apply',
    name: 'CertificationApply',
    component: () => import('@/views/certification/Apply.vue'),
    meta: { title: '申请认证', requiresAuth: true }
  },
  {
    path: '/cs',
    name: 'CsHistory',
    component: () => import('@/views/cs/History.vue'),
    meta: { title: '客服会话', requiresAuth: true }
  },
  {
    path: '/certification/providers',
    name: 'CertificationProviders',
    component: () => import('@/views/certification/Providers.vue'),
    meta: { title: '认证服务商' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
