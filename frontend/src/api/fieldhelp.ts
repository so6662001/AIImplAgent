import request from '@/utils/request'
import type { R } from '@/types'
export function createFieldHelp(data: Record<string, unknown>) { return request.post<R<any>>('/field-help', data) }
export function getPageHelps(page: string) { return request.get<R<any[]>>('/field-help', { params: { page } }) }
export function lookupFieldHelp(page: string, field: string) { return request.get<R<any>>('/field-help/lookup', { params: { page, field } }) }
