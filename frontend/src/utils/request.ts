import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { R } from '@/types'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const data = res.data as R<unknown>
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return res
  },
  (err) => {
    if (err.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(err)
    }
    const msg = err.response?.data?.message || err.message || '网络异常'
    ElMessage.error(msg)
    return Promise.reject(err)
  },
)

export default request
