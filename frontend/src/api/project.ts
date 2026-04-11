import request from '@/utils/request'
import type { R, Project } from '@/types'

export function createProject(data: Record<string, unknown>) {
  return request.post<R<Project>>('/projects', data)
}

export function getProject(id: number) {
  return request.get<R<Project>>(`/projects/${id}`)
}

export function listProjects() {
  return request.get<R<Project[]>>('/projects')
}
