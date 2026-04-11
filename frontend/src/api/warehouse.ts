import request from '@/utils/request'
import type { R, Warehouse } from '@/types'

export function createWarehouse(data: Record<string, unknown>) {
  return request.post<R<Warehouse>>('/warehouses', data)
}

export function listWarehouses() {
  return request.get<R<Warehouse[]>>('/warehouses')
}
