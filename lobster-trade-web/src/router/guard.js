import { useUserStore } from '@/stores/user'

export function setupRouterGuard(router) {
  router.beforeEach(async (to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 龙虾道具交易平台` : '龙虾道具交易平台'

    const userStore = useUserStore()

    // 需要登录
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 已登录访问游客页面（登录/注册）
    if (to.meta.guest && userStore.isLoggedIn) {
      next('/home')
      return
    }

    next()
  })
}
