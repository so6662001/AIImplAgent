import request from '@/utils/request'
import type { R, TraineeProfile } from '@/types'

export function createTrainee(data: Record<string, unknown>) {
  return request.post<R<TraineeProfile>>('/trainees', data)
}

export function listTrainees(projectId: number) {
  return request.get<R<TraineeProfile[]>>('/trainees', { params: { projectId } })
}
