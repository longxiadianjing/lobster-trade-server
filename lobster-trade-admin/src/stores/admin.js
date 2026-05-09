import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    info: JSON.parse(localStorage.getItem('admin_info') || 'null') || null,
    permissions: (() => {
      try {
        const stored = localStorage.getItem('admin_permissions')
        if (stored) {
          const parsed = JSON.parse(stored)
          // 可能是字符串 '*' 或数组 ['USER_VIEW', ...]
          if (parsed === '*' || (Array.isArray(parsed) && parsed.includes('*'))) return '*'
          if (Array.isArray(parsed)) return parsed
        }
      } catch (e) {}
      return []
    })()
  }),

  actions: {
    async login(username, password) {
      const res = await request.post('/admin/login', { username, password })
      this.token = res.data?.token
      this.info = res.data?.admin
      if (this.token) {
        localStorage.setItem('admin_token', this.token)
        localStorage.setItem('admin_info', JSON.stringify(this.info))
        // 超级管理员权限是 '*'，其他是逗号分隔的字符串
        const perms = res.data?.admin?.permissions
        if (perms) {
          this.permissions = perms
          localStorage.setItem('admin_permissions', JSON.stringify(perms))
        }
      }
      return res
    },

    logout() {
      this.token = ''
      this.info = null
      this.permissions = []
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
      localStorage.removeItem('admin_permissions')
    },

    async fetchInfo() {
      try {
        const res = await request.get('/admin/info')
        this.info = res.data
        localStorage.setItem('admin_info', JSON.stringify(this.info))
        if (res.data?.permissions) {
          this.permissions = res.data.permissions
          localStorage.setItem('admin_permissions', JSON.stringify(res.data.permissions))
        }
      } catch (e) {
        // ignore
      }
    }
  }
})
