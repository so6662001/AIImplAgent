import request from '@/utils/request'
import type { R, ProductCategory } from '@/types'

export function createCategory(data: Record<string, unknown>) {
  return request.post<R<ProductCategory>>('/categories', data)
}

export function listCategories() {
  return request.get<R<ProductCategory[]>>('/categories')
}
