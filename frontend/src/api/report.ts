import request from '@/utils/request'
import type { R, DeliveryReport } from '@/types'

export function createDeliveryReport(data: Record<string, unknown>) {
  return request.post<R<DeliveryReport>>('/delivery-reports', data)
}

export function listDeliveryReports(projectId: number) {
  return request.get<R<DeliveryReport[]>>('/delivery-reports', { params: { projectId } })
}

export function confirmDeliveryReport(id: number, confirmedBy: string) {
  return request.put<R<DeliveryReport>>(`/delivery-reports/${id}/confirm`, { confirmedBy })
}
