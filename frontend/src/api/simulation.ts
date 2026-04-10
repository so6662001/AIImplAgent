import request from '@/utils/request'
import type { R, SimulationScene } from '@/types'

export function createScene(data: Record<string, unknown>) {
  return request.post<R<SimulationScene>>('/simulation-scenes', data)
}

export function listScenes(projectId: number) {
  return request.get<R<SimulationScene[]>>('/simulation-scenes', { params: { projectId } })
}

export function executeScene(id: number, data: Record<string, unknown>) {
  return request.put<R<SimulationScene>>(`/simulation-scenes/${id}/execute`, data)
}
