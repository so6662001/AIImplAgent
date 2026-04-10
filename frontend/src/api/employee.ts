import request from '@/utils/request'
import type { R, EmployeeRecord } from '@/types'

export function createEmployee(data: Record<string, unknown>) {
  return request.post<R<EmployeeRecord>>('/employees', data)
}

export function listEmployees() {
  return request.get<R<EmployeeRecord[]>>('/employees')
}
