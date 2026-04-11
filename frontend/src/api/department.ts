import request from '@/utils/request'
import type { R, Department } from '@/types'

export function createDepartment(data: Record<string, unknown>) {
  return request.post<R<Department>>('/departments', data)
}

export function listDepartments() {
  return request.get<R<Department[]>>('/departments')
}
