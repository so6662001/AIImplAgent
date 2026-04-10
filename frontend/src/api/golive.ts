import request from '@/utils/request'
import type { R, GoLiveCheckItem } from '@/types'

export function initChecklist(projectId: number) {
  return request.post<R<GoLiveCheckItem[]>>(`/go-live-checks/init/${projectId}`)
}

export function listCheckItems(projectId: number) {
  return request.get<R<GoLiveCheckItem[]>>('/go-live-checks', { params: { projectId } })
}

export function updateCheckItem(id: number, data: Record<string, unknown>) {
  return request.put<R<GoLiveCheckItem>>(`/go-live-checks/${id}`, data)
}

export function getReadiness(projectId: number) {
  return request.get<R<{ ready: boolean; message: string; passCount: number; totalCount: number }>>(`/go-live-checks/readiness/${projectId}`)
}
