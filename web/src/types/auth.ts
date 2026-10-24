export type UserRole = 'CUSTOMER' | 'WORKER' | 'ADMIN'

export interface UserInfo {
  id: number
  name: string
  phone: string
  role: UserRole
  avatar: string | null
}

export interface LoginPayload {
  phone: string
  code: string
  role: UserRole
}

export interface LoginResult {
  token: string
  expiresIn: number
  user: UserInfo
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  traceId: string
}
