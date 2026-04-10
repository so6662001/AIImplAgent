import request from '@/utils/request'
import type { R, CustomerBalance } from '@/types'

export function createCustomerBalance(data: Record<string, unknown>) {
  return request.post<R<CustomerBalance>>('/customer-balances', data)
}

export function listCustomerBalances(projectId: number) {
  return request.get<R<CustomerBalance[]>>('/customer-balances', { params: { projectId } })
}
