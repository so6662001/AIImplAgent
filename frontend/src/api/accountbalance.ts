import request from '@/utils/request'
import type { R, AccountBalance } from '@/types'

export function createAccountBalance(data: Record<string, unknown>) {
  return request.post<R<AccountBalance>>('/account-balances', data)
}

export function listAccountBalances(projectId: number) {
  return request.get<R<AccountBalance[]>>('/account-balances', { params: { projectId } })
}
