import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/product/:id',
    name: 'productDetail',
    component: () => import('@/views/ProductDetail.vue')
  },
  {
    path: '/order/:id',
    name: 'orderDetail',
    component: () => import('@/views/OrderDetail.vue')
  },
  {
    path: '/center',
    name: 'userCenter',
    component: () => import('@/views/UserCenter.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/Register.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const publicPages = ['/login', '/register']
  if (!userStore.token && !publicPages.includes(to.path)) {
    next('/login')
  } else {
    next()
  }
})

export default router