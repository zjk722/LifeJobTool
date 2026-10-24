import { request } from './http'
import type { LoginPayload, LoginResult, UserInfo } from '@/types/auth'

export function sendSmsCode(phone: string): Promise<void> {
  return request<void>({
    url: '/auth/sms/send',
    method: 'post',
    data: { phone },
  })
}

export function loginBySms(payload: LoginPayload): Promise<LoginResult> {
  return request<LoginResult>({
    url: '/auth/login/sms',
    method: 'post',
    data: payload,
  })
}

export function getMe(): Promise<UserInfo> {
  return request<UserInfo>({
    url: '/auth/me',
    method: 'get',
  })
}

export function logout(): Promise<void> {
  return request<void>({
    url: '/auth/logout',
    method: 'post',
  })
}
