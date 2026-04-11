import request from '@/utils/request'
import type { R } from '@/types'
export function logAssist(data: Record<string, unknown>) { return request.post<R<any>>('/input-assist/logs', data) }
export function getAssistStats(page: string) { return request.get<R<any>>('/input-assist/logs/stats', { params: { page } }) }
export function listAssistLogs(page: string) { return request.get<R<any[]>>('/input-assist/logs', { params: { page } }) }
