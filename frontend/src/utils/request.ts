import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { R } from '@/types'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
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
    const msg = err.response?.data?.message || err.message || '网络异常'
    ElMessage.error(msg)
    return Promise.reject(err)
  },
)

export default request
