import request from '@/utils/request'
import type { R, InventoryBalance } from '@/types'

export function createInventoryBalance(data: Record<string, unknown>) {
  return request.post<R<InventoryBalance>>('/inventory-balances', data)
}

export function listInventoryBalances(projectId: number) {
  return request.get<R<InventoryBalance[]>>('/inventory-balances', { params: { projectId } })
}
