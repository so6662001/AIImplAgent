import request from '@/utils/request'
import type { R, Product, Customer } from '@/types'

export function createProduct(data: Record<string, unknown>) {
  return request.post<R<Product>>('/products', data)
}

export function getProduct(id: number) {
  return request.get<R<Product>>(`/products/${id}`)
}

export function listProducts() {
  return request.get<R<Product[]>>('/products')
}

export function createCustomer(data: Record<string, unknown>) {
  return request.post<R<Customer>>('/customers', data)
}

export function getCustomer(id: number) {
  return request.get<R<Customer>>(`/customers/${id}`)
}

export function listCustomers() {
  return request.get<R<Customer[]>>('/customers')
}
