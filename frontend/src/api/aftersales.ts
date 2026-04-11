import request from '@/utils/request'
import type { R } from '@/types'
export function createAfterSalesTicket(data: Record<string, unknown>) { return request.post<R<any>>('/after-sales-tickets', data) }
export function listAfterSalesTickets(projectId: number) { return request.get<R<any[]>>('/after-sales-tickets', { params: { projectId } }) }
export function resolveAfterSalesTicket(id: number, data: Record<string, unknown>) { return request.put<R<any>>(`/after-sales-tickets/${id}/resolve`, data) }
export function rateAfterSalesTicket(id: number, satisfaction: number) { return request.put<R<any>>(`/after-sales-tickets/${id}/rate`, null, { params: { satisfaction } }) }
export function getAfterSalesStats(projectId: number) { return request.get<R<any>>(`/after-sales-tickets/stats/${projectId}`) }
