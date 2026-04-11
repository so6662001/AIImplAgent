import request from '@/utils/request'
import type { R } from '@/types'
export function runHealthCheck(projectId: number) { return request.post<R<any[]>>(`/alerts/health-check/${projectId}`) }
export function listAlerts(projectId: number) { return request.get<R<any[]>>('/alerts', { params: { projectId } }) }
export function acknowledgeAlert(id: number, acknowledgedBy: string) { return request.put<R<any>>(`/alerts/${id}/acknowledge`, null, { params: { acknowledgedBy } }) }
