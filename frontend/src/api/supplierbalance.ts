import request from '@/utils/request'
import type { R, SupplierBalance } from '@/types'

export function createSupplierBalance(data: Record<string, unknown>) {
  return request.post<R<SupplierBalance>>('/supplier-balances', data)
}

export function listSupplierBalances(projectId: number) {
  return request.get<R<SupplierBalance[]>>('/supplier-balances', { params: { projectId } })
}
