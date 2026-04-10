import request from '@/utils/request'
import type { R, OtherReceivable } from '@/types'

export function createOtherReceivable(data: Record<string, unknown>) {
  return request.post<R<OtherReceivable>>('/other-receivables', data)
}

export function listOtherReceivables(projectId: number) {
  return request.get<R<OtherReceivable[]>>('/other-receivables', { params: { projectId } })
}
