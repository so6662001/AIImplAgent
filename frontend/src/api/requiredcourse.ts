import request from '@/utils/request'
import type { R, RequiredCourse } from '@/types'

export function listRequiredCourses(industryType?: string) {
  return request.get<R<RequiredCourse[]>>('/required-courses', { params: { industryType } })
}
