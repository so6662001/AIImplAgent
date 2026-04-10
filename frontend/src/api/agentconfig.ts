import request from '@/utils/request'
import type { R, AgentConfig } from '@/types'

export function createAgentConfig(data: Record<string, unknown>) {
  return request.post<R<AgentConfig>>('/agent-configs', data)
}

export function listAgentConfigs() {
  return request.get<R<AgentConfig[]>>('/agent-configs')
}

export function updateAgentConfig(id: number, data: Record<string, unknown>) {
  return request.put<R<AgentConfig>>(`/agent-configs/${id}`, data)
}
