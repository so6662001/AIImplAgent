import request from '@/utils/request'
import type { R } from '@/types'

export function getRecommendation(data: Record<string, unknown>) {
  return request.post<R<any>>('/dispatch/recommend', data)
}

export function assignPm(data: { projectId: number; engineerId: number }) {
  return request.post<R<any>>('/dispatch/assign', data)
}
