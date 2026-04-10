import request from '@/utils/request'
import type { R, InvoiceBalance } from '@/types'

export function createInvoiceBalance(data: Record<string, unknown>) {
  return request.post<R<InvoiceBalance>>('/invoice-balances', data)
}

export function listInvoiceBalances(projectId: number, invoiceType?: string) {
  return request.get<R<InvoiceBalance[]>>('/invoice-balances', { params: { projectId, invoiceType } })
}
