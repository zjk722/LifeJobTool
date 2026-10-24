import type { UserInfo } from '@/types/auth'

const TOKEN_KEY = 'hs_token'
const USER_KEY = 'hs_user'
const LAST_PHONE_KEY = 'hs_last_phone'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getStoredUser(): UserInfo | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as UserInfo
  } catch {
    return null
  }
}

export function setStoredUser(user: UserInfo): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearStoredUser(): void {
  localStorage.removeItem(USER_KEY)
}

export function getLastPhone(): string {
  return localStorage.getItem(LAST_PHONE_KEY) ?? ''
}

export function setLastPhone(phone: string): void {
  localStorage.setItem(LAST_PHONE_KEY, phone)
}
