import request from '@/utils/request'
import type { R, Supplier } from '@/types'

export function createSupplier(data: Record<string, unknown>) {
  return request.post<R<Supplier>>('/suppliers', data)
}

export function listSuppliers() {
  return request.get<R<Supplier[]>>('/suppliers')
}
