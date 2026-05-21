import axios from 'axios'
import { Toast } from 'vant'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
  baseURL: '',
  timeout: 30000
})

service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code && res.code !== 0 && res.code !== 200) {
      Toast.fail(res.message || '请求失败')
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
        router.push('/login')
        Toast.fail('登录已过期，请重新登录')
      } else if (status === 403) {
        router.push('/login')
        return Promise.reject(new Error('权限不足'))
      } else {
        Toast.fail(data?.message || '请求失败')
      }
    } else {
      Toast.fail('网络错误')
    }
    return Promise.reject(error)
  }
)

export default service