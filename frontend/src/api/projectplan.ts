import request from '@/utils/request'
import type { R } from '@/types'

export function createProjectPlan(data: Record<string, unknown>) {
  return request.post<R<any>>('/project-plans', data)
}
export function listProjectPlans(projectId: number) {
  return request.get<R<any[]>>('/project-plans', { params: { projectId } })
}
export function generatePlan(data: Record<string, unknown>) {
  return request.post<R<any>>('/project-plans/generate', data)
}
export function getPlanDetail(id: number) {
  return request.get<R<any>>(`/project-plans/${id}/detail`)
}
