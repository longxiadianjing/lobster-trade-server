import { defineStore } from 'pinia'
import { getUserInfo } from '@/api/user'
import { getUnreadCount } from '@/api/notification'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    unreadNotificationCount: 0
  }),

  getters: {
    isLoggedIn: state => !!state.token,
    userId: state => state.userInfo?.id,
    nickname: state => state.userInfo?.nickname || '未登录',
    avatar: state => state.userInfo?.avatar || '',
    realNameStatus: state => state.userInfo?.real_name_status || 0
  },

  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    async fetchUserInfo() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
        return res.data
      } catch (error) {
        console.error('Failed to fetch user info:', error)
        throw error
      }
    },

    async fetchUnreadCount() {
      try {
        const res = await getUnreadCount()
        this.unreadNotificationCount = res?.data ?? res ?? 0
      } catch (error) {
        console.error('Failed to fetch unread count:', error)
        this.unreadNotificationCount = 0
      }
    },

    setUnreadCount(count) {
      this.unreadNotificationCount = count
    },

    logout() {
      this.token = ''
      this.userInfo = null
      this.unreadNotificationCount = 0
      localStorage.removeItem('token')
    }
  }
})