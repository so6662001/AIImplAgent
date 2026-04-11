import request from '@/utils/request'
import type { R, RelatedUnit } from '@/types'

export function createRelatedUnit(data: Record<string, unknown>) {
  return request.post<R<RelatedUnit>>('/related-units', data)
}

export function listRelatedUnits() {
  return request.get<R<RelatedUnit[]>>('/related-units')
}
