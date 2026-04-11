import request from '@/utils/request'
import type { R, EngineerWorklog } from '@/types'

export function createWorklog(data: Record<string, unknown>) {
  return request.post<R<EngineerWorklog>>('/engineer-worklogs', data)
}

export function listWorklogs(params?: { engineerId?: number; projectId?: number }) {
  return request.get<R<EngineerWorklog[]>>('/engineer-worklogs', { params })
}

export function submitWorklog(id: number) {
  return request.put<R<EngineerWorklog>>(`/engineer-worklogs/${id}/submit`)
}

export function getDailyTasks(params: { engineerId: number; projectId: number; date: string }) {
  return request.get<R<any>>('/workforce/daily-tasks', { params })
}
export function getReportDraft(params: { engineerId: number; projectId: number; date: string }) {
  return request.get<R<any>>('/workforce/report-draft', { params })
}
