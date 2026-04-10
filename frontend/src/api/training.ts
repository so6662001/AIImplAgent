import request from '@/utils/request'
import type { R, ExamRecord, GoLiveCheck } from '@/types'

export function submitExam(data: Record<string, unknown>) {
  return request.post<R<ExamRecord>>('/exams', data)
}

export function checkGoLive(projectId: number) {
  return request.get<R<GoLiveCheck>>(`/exams/go-live-check/${projectId}`)
}
