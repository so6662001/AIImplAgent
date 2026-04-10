import request from '@/utils/request'
import type { R, SubjectBalance, TrialBalance } from '@/types'

export function createSubjectBalance(data: Record<string, unknown>) {
  return request.post<R<SubjectBalance>>('/subject-balances', data)
}

export function listSubjectBalances(projectId: number) {
  return request.get<R<SubjectBalance[]>>('/subject-balances', { params: { projectId } })
}

export function getTrialBalance(projectId: number) {
  return request.get<R<TrialBalance>>('/subject-balances/trial-balance', { params: { projectId } })
}
