import request from '@/utils/request'
import type { R, LoginVO, SysUser } from '@/types'

export function login(username: string, password: string) {
  return request.post<R<LoginVO>>('/auth/login', { username, password })
}

export function register(data: Record<string, unknown>) {
  return request.post<R<SysUser>>('/auth/register', data)
}

export function getMe() {
  return request.get<R<SysUser>>('/auth/me')
}
