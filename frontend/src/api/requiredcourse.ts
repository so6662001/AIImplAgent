import request from '@/utils/request'
import type { R, RequiredCourse } from '@/types'

export function listRequiredCourses(industryType?: string) {
  return request.get<R<RequiredCourse[]>>('/required-courses', {
    params: industryType ? { industryType } : {},
  })
}

export function createRequiredCourse(data: Record<string, unknown>) {
  return request.post<R<RequiredCourse>>('/required-courses', data)
}

export function updateRequiredCourse(id: number, data: Record<string, unknown>) {
  return request.put<R<RequiredCourse>>(`/required-courses/${id}`, data)
}

export function deleteRequiredCourse(id: number) {
  return request.delete<R<void>>(`/required-courses/${id}`)
}

export function batchToggle(data: {
  industryType: string
  courseModules: string[]
  kaRequired: boolean
}) {
  return request.post<R<void>>('/required-courses/batch-toggle', data)
}
