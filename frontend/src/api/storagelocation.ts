import request from '@/utils/request'
import type { R, StorageLocation } from '@/types'

export function createStorageLocation(data: Record<string, unknown>) {
  return request.post<R<StorageLocation>>('/storage-locations', data)
}

export function listStorageLocations(warehouseId?: number) {
  return request.get<R<StorageLocation[]>>('/storage-locations', { params: { warehouseId } })
}
