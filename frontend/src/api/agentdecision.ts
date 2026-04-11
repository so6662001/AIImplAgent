import request from '@/utils/request'
import type { R } from '@/types'

export function listDecisions(params?: Record<string, any>) {
  return request.get<R<any[]>>('/agent-decisions', { params })
}

export function reviewDecision(id: number, data: { reviewResult: string; reviewComment?: string }) {
  return request.put<R<any>>(`/agent-decisions/${id}/review`, null, { params: data })
}

export function getAgentPerformance(agentCode: string) {
  return request.get<R<any>>(`/agent-decisions/performance/${agentCode}`)
}

export function getAllPerformance() {
  return request.get<R<any[]>>('/agent-decisions/performance')
}
