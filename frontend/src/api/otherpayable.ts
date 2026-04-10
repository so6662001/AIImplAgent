import request from '@/utils/request'
import type { R, OtherPayable } from '@/types'

export function createOtherPayable(data: Record<string, unknown>) {
  return request.post<R<OtherPayable>>('/other-payables', data)
}

export function listOtherPayables(projectId: number) {
  return request.get<R<OtherPayable[]>>('/other-payables', { params: { projectId } })
}
