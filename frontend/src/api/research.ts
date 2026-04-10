import request from '@/utils/request'
import type { R, CustomerProfile } from '@/types'

export function createCustomerProfile(data: Record<string, unknown>) {
  return request.post<R<CustomerProfile>>('/customer-profiles', data)
}

export function listCustomerProfiles(projectId: number) {
  return request.get<R<CustomerProfile[]>>(`/customer-profiles`, { params: { projectId } })
}
