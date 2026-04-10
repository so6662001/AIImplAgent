import request from '@/utils/request'
import type { R, AccountSubject } from '@/types'

export function createSubject(data: Record<string, unknown>) {
  return request.post<R<AccountSubject>>('/account-subjects', data)
}

export function listSubjects() {
  return request.get<R<AccountSubject[]>>('/account-subjects')
}
