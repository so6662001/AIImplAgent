import request from '@/utils/request'
import type { R, ServerProfile } from '@/types'

export function createServerProfile(data: Record<string, unknown>) {
  return request.post<R<ServerProfile>>('/server-profiles', data)
}

export function listServerProfiles(projectId: number) {
  return request.get<R<ServerProfile[]>>('/server-profiles', { params: { projectId } })
}

export function healthCheck(id: number) {
  return request.post<R<ServerProfile>>(`/server-profiles/${id}/health-check`)
}
