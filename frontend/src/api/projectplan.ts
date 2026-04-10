import request from '@/utils/request'
import type { R, ProjectPlan } from '@/types'

export function createProjectPlan(data: Record<string, unknown>) {
  return request.post<R<ProjectPlan>>('/project-plans', data)
}

export function listProjectPlans(projectId: number) {
  return request.get<R<ProjectPlan[]>>('/project-plans', { params: { projectId } })
}
