import request from '@/utils/request'
import type { R } from '@/types'

export function initImportProgress(projectId: number) { return request.post<R<any>>(`/data-import/init/${projectId}`) }
export function getImportProgress(projectId: number) { return request.get<R<any>>(`/data-import/progress/${projectId}`) }
export function completeBatch(projectId: number, batchNumber: number) { return request.post<R<any>>(`/data-import/complete-batch/${projectId}/${batchNumber}`) }
export function advanceBatch(projectId: number) { return request.post<R<any>>(`/data-import/advance/${projectId}`) }
export function getReconciliation(projectId: number) { return request.get<R<any>>(`/data-import/reconciliation/${projectId}`) }
export function getImportOverview(projectId: number) { return request.get<R<any>>(`/data-import/overview/${projectId}`) }
