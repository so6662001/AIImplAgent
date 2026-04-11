import request from '@/utils/request'
import type { R, Engineer, ProjectEvaluation } from '@/types'

export function createEngineer(data: Record<string, unknown>) {
  return request.post<R<Engineer>>('/engineers', data)
}

export function listEngineers() {
  return request.get<R<Engineer[]>>('/engineers')
}

export function createProjectEvaluation(data: Record<string, unknown>) {
  return request.post<R<ProjectEvaluation>>('/project-evaluations', data)
}

export function listProjectEvaluations(projectId: number) {
  return request.get<R<ProjectEvaluation[]>>('/project-evaluations', { params: { projectId } })
}

export function getWorkforceDashboard() { return request.get<R<any>>('/workforce/dashboard') }
export function getCompositeScore(engineerId: number) { return request.get<R<any>>(`/workforce/composite-score/${engineerId}`) }
export function getMonthlyReport() { return request.get<R<any>>('/workforce/monthly-report') }
