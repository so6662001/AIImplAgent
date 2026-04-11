import request from '@/utils/request'
import clientRequest from '@/utils/clientRequest'
import type { R, QaSession, QaMessage } from '@/types'

export function listQaSessions(projectId: number) {
  return request.get<R<QaSession[]>>('/qa-sessions', { params: { projectId } })
}

export function getSessionMessages(sessionId: number) {
  return request.get<R<QaMessage[]>>(`/qa-sessions/${sessionId}/messages`)
}

export function replyToSession(sessionId: number, data: { content: string; relatedModule?: string; relatedVideoUrl?: string }) {
  return request.post<R<QaMessage>>(`/qa-sessions/${sessionId}/reply`, data)
}

export function clientAsk(data: { sessionId?: number; question: string; currentPage?: string; currentField?: string }) {
  return clientRequest.post<R<QaMessage>>('/qa/ask', data)
}

export function clientGetSessions() {
  return clientRequest.get<R<QaSession[]>>('/qa/sessions')
}

export function clientGetMessages(sessionId: number) {
  return clientRequest.get<R<QaMessage[]>>(`/qa/sessions/${sessionId}/messages`)
}

export function clientRateMessage(messageId: number, helpful: boolean) {
  return clientRequest.post<R<void>>(`/qa/messages/${messageId}/rate`, { helpful })
}

export function clientLogin(projectCode: string, employeeName: string) {
  return clientRequest.post<R<{ accessToken: string; projectId: number; projectName: string; employeeName: string; role: string }>>('/auth/login', { projectCode, employeeName })
}
