import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const adminToken = localStorage.getItem('adminToken')
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    } else if (adminToken) {
      config.headers['Authorization'] = `Bearer ${adminToken}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code && res.code !== 0 && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        // 如果是管理后台页面则跳转管理员登录
        if (router.currentRoute.value.path.startsWith('/admin')) {
          localStorage.removeItem('adminToken')
          router.push('/admin/login')
        } else {
          router.push('/login')
        }
        ElMessage.error('登录已过期，请重新登录')
      } else {
        ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
