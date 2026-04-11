import request from '@/utils/request'
import type { R } from '@/types'
export function completeLlm(data: Record<string, unknown>) { return request.post<R<any>>('/llm-gateway/complete', data) }
export function testLlm(agentCode: string) { return request.get<R<any>>(`/llm-gateway/test/${agentCode}`) }
