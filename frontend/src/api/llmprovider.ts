import request from '@/utils/request'
import type { R, LlmProviderConfig } from '@/types'

export function createProvider(data: Record<string, unknown>) {
  return request.post<R<LlmProviderConfig>>('/llm-providers', data)
}

export function listProviders() {
  return request.get<R<LlmProviderConfig[]>>('/llm-providers')
}

export function updateProvider(id: number, data: Record<string, unknown>) {
  return request.put<R<LlmProviderConfig>>(`/llm-providers/${id}`, data)
}

export function deleteProvider(id: number) {
  return request.delete<R<void>>(`/llm-providers/${id}`)
}
