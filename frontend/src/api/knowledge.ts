import request from '@/utils/request'
import type { R } from '@/types'

export function createKnowledge(data: Record<string, unknown>) {
  return request.post<R<any>>('/knowledge', data)
}

export function listKnowledge(params?: Record<string, any>) {
  return request.get<R<any[]>>('/knowledge', { params })
}

export function getKnowledge(id: number) {
  return request.get<R<any>>(`/knowledge/${id}`)
}

export function searchKnowledge(q: string, category?: string, projectId?: number) {
  return request.get<R<any[]>>('/knowledge/search', { params: { q, category, projectId } })
}

export function markHelpful(id: number) {
  return request.post<R<any>>(`/knowledge/${id}/helpful`)
}

export function getKnowledgeStats() {
  return request.get<R<any>>('/knowledge/stats')
}

export function importFromTicket(ticketId: number) {
  return request.post<R<any>>(`/knowledge/import-from-ticket/${ticketId}`)
}
