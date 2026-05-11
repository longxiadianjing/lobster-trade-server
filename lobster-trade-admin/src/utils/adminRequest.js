import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const adminRequest = axios.create({
  baseURL: '/api',
  timeout: 30000
})

adminRequest.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

adminRequest.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code && res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else if (error.response && error.response.status === 403) {
      router.push('/login')
      ElMessage.error('无权限访问，请联系管理员')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default adminRequest