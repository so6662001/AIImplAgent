import clientRequest from '@/utils/clientRequest'
import type { R } from '@/types'

export function getCoursesWithProgress() { return clientRequest.get<R<any[]>>('/learning/courses') }
export function getCourseWithProgress(courseId: number) { return clientRequest.get<R<any>>(`/learning/courses/${courseId}`) }
export function updateLearningProgress(data: Record<string, unknown>) { return clientRequest.post<R<any>>('/learning/progress', data) }
export function getLearningSummary() { return clientRequest.get<R<any>>('/learning/summary') }
