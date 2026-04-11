import request from '@/utils/request'
import type { R } from '@/types'
export function checkCustomerHealth(projectId: number) { return request.post<R<any>>(`/customer-health/check/${projectId}`) }
export function getLatestHealth(projectId: number) { return request.get<R<any>>('/customer-health', { params: { projectId } }) }
export function getHealthHistory(projectId: number) { return request.get<R<any[]>>(`/customer-health/history/${projectId}`) }
