import request from '@/utils/request'
import type { R } from '@/types'
export function getProjectAnalytics(projectId: number) { return request.get<R<any>>(`/analytics/project/${projectId}`) }
export function getDepartmentAnalytics() { return request.get<R<any>>('/analytics/department') }
