import request from '@/utils/request'
import type { R, BankAccount } from '@/types'

export function createBankAccount(data: Record<string, unknown>) {
  return request.post<R<BankAccount>>('/bank-accounts', data)
}

export function listBankAccounts() {
  return request.get<R<BankAccount[]>>('/bank-accounts')
}
