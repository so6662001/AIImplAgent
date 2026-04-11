import request from '@/utils/request'
import type { R, TrainingDashboard } from '@/types'

export function getTrainingDashboard(projectId: number) {
  return request.get<R<TrainingDashboard>>('/training-dashboard', { params: { projectId } })
}
