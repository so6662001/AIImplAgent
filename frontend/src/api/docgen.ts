import request from '@/utils/request'
import type { R } from '@/types'
export function generateDocument(data: Record<string, unknown>) { return request.post<R<any>>('/documents/generate', data) }
export function listDocumentTypes(projectId: number) { return request.get<R<any[]>>(`/documents/types/${projectId}`) }
