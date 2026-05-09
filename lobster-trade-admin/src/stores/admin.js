import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    info: JSON.parse(localStorage.getItem('admin_info') || 'null') || null,
    permissions: JSON.parse(localStorage.getItem('admin_permissions') || '""') || []
  }),

  actions: {
    async login(username, password) {
      const res = await request.post('/admin/login', { username, password })
      this.token = res.data?.token
      this.info = res.data?.admin
      if (this.token) {
        localStorage.setItem('admin_token', this.token)
        localStorage.setItem('admin_info', JSON.stringify(this.info))
        if (res.data?.permissions) {
          this.permissions = res.data.permissions
          localStorage.setItem('admin_permissions', JSON.stringify(res.data.permissions))
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