import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { R } from '@/types'
import router from '@/router'

const CLIENT_TOKEN_KEY = 'client_qa_auth'

const clientRequest = axios.create({
  baseURL: '/api/client',
  timeout: 30000,
})

clientRequest.interceptors.request.use((config) => {
  try {
    const raw = localStorage.getItem(CLIENT_TOKEN_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (parsed.accessToken) {
        config.headers['X-Client-Token'] = parsed.accessToken
      }
    }
  } catch { /* ignore */ }
  return config
})

clientRequest.interceptors.response.use(
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
      localStorage.removeItem(CLIENT_TOKEN_KEY)
      router.push('/client/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(err)
    }
    const msg = err.response?.data?.message || err.message || '网络异常'
    ElMessage.error(msg)
    return Promise.reject(err)
  },
)

export default clientRequest
