import request from '@/utils/request'
import type { R } from '@/types'

export function createScene(data: Record<string, unknown>) {
  return request.post<R<any>>('/simulation-scenes', data)
}
export function listScenes(projectId: number) {
  return request.get<R<any[]>>('/simulation-scenes', { params: { projectId } })
}
export function executeScene(id: number, data: Record<string, unknown>) {
  return request.put<R<any>>(`/simulation-scenes/${id}/execute`, data)
}
export function generateMockData(projectId: number) {
  return request.post<R<any>>(`/simulation/mock-data/generate/${projectId}`)
}
export function autoGenerateScenes(projectId: number) {
  return request.post<R<any[]>>(`/simulation/scenes/auto-generate/${projectId}`)
}
export function getSimulationReport(projectId: number) {
  return request.get<R<any>>(`/simulation/reports/${projectId}`)
}
