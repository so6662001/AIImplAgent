import request from '@/utils/request'
import type { R, CustomerProfile } from '@/types'

export function createCustomerProfile(data: Record<string, unknown>) {
  return request.post<R<CustomerProfile>>('/customer-profiles', data)
}

export function listCustomerProfiles(projectId: number) {
  return request.get<R<CustomerProfile[]>>(`/customer-profiles`, { params: { projectId } })
}

export function generateQuestionnaire(data: { industryType: string; scale: string; modules: string[] }) {
  return request.post<R<any>>('/research/questionnaire/generate', data)
}

export function generateReport(profileId: number) {
  return request.post<R<any>>(`/research/reports/generate/${profileId}`)
}

export function listReports(projectId: number) {
  return request.get<R<any[]>>('/research/reports', { params: { projectId } })
}
