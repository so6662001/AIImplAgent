import request from '@/utils/request'
import type { R, EngineerWorklog } from '@/types'

export function createWorklog(data: Record<string, unknown>) {
  return request.post<R<EngineerWorklog>>('/worklogs', data)
}

export function listWorklogs(params?: { engineerId?: number; projectId?: number }) {
  return request.get<R<EngineerWorklog[]>>('/worklogs', { params })
}

export function submitWorklog(id: number) {
  return request.put<R<EngineerWorklog>>(`/worklogs/${id}/submit`)
}
