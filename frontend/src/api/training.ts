import request from '@/utils/request'
import type { R, ExamRecord, GoLiveCheck } from '@/types'

export function submitExam(data: Record<string, unknown>) {
  return request.post<R<ExamRecord>>('/exams', data)
}

export function checkGoLive(projectId: number) {
  return request.get<R<GoLiveCheck>>(`/exams/go-live-check/${projectId}`)
}

export function generateExam(data: { projectId: number; module: string; difficulty: string }) {
  return request.post<R<any>>('/training/exam-generator/generate', data)
}
export function getWeakPoints(projectId: number, traineeId: number) {
  return request.get<R<any>>(`/training/weak-points/${projectId}/${traineeId}`)
}
export function getEnhancedDashboard(projectId: number) {
  return request.get<R<any>>(`/training-dashboard/${projectId}/enhanced`)
}
