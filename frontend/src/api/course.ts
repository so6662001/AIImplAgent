import request from '@/utils/request'
import type { R } from '@/types'

export function createCourse(data: Record<string, unknown>) { return request.post<R<any>>('/courses', data) }
export function listCourses() { return request.get<R<any[]>>('/courses') }
export function getCourseDetail(id: number) { return request.get<R<any>>(`/courses/${id}`) }
export function createChapter(courseId: number, data: Record<string, unknown>) { return request.post<R<any>>(`/courses/${courseId}/chapters`, data) }
export function listChapters(courseId: number) { return request.get<R<any[]>>(`/courses/${courseId}/chapters`) }
