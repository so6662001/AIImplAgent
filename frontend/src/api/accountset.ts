import request from '@/utils/request'
import type { R, AccountSet } from '@/types'

export function createAccountSet(data: Record<string, unknown>) {
  return request.post<R<AccountSet>>('/account-sets', data)
}

export function getAccountSet(id: number) {
  return request.get<R<AccountSet>>(`/account-sets/${id}`)
}

export function listAccountSets(projectId: number) {
  return request.get<R<AccountSet[]>>('/account-sets', { params: { projectId } })
}

export function activateAccountSet(id: number) {
  return request.put<R<AccountSet>>(`/account-sets/${id}/activate`)
}
