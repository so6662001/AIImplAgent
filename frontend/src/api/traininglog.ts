import request from '@/utils/request'
import type { R, TrainingDailyLog } from '@/types'

export function createTrainingLog(data: Record<string, unknown>) {
  return request.post<R<TrainingDailyLog>>('/training-logs', data)
}

export function listTrainingLogs(projectId: number) {
  return request.get<R<TrainingDailyLog[]>>('/training-logs', { params: { projectId } })
}

export function updateTrainingLog(id: number, data: Record<string, unknown>) {
  return request.put<R<TrainingDailyLog>>(`/training-logs/${id}`, data)
}
