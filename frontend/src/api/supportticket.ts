import request from '@/utils/request'
import type { R } from '@/types'
export function createTicket(data: Record<string, unknown>) { return request.post<R<any>>('/support-tickets', data) }
export function listTickets(projectId: number) { return request.get<R<any[]>>('/support-tickets', { params: { projectId } }) }
export function resolveTicket(id: number, resolution: string) { return request.put<R<any>>(`/support-tickets/${id}/resolve`, null, { params: { resolution } }) }
export function getTicketStats(projectId: number) { return request.get<R<any>>(`/support-tickets/stats/${projectId}`) }
