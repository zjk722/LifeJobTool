import { AxiosError } from 'axios'
import type { AxiosAdapter, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { http } from './http'
import type { ApiResponse, LoginResult, UserInfo } from '@/types/auth'
import { getStoredUser } from '@/utils/auth-storage'

const MOCK_CODE = '123456'

let mockUser: UserInfo | null = null

function ok<T>(data: T): ApiResponse<T> {
  return { code: 0, message: 'ok', data, traceId: 'mock' }
}

function fail<T>(code: number, message: string): ApiResponse<T | null> {
  return { code, message, data: null, traceId: 'mock' }
}

function wait(ms = 480): Promise<void> {
  return new Promise((resolve) => window.setTimeout(resolve, ms))
}

const adapter: AxiosAdapter = async (config: InternalAxiosRequestConfig) => {
  const url = config.url ?? ''
  const method = (config.method ?? 'get').toUpperCase()
  const payload = config.data
    ? typeof config.data === 'string'
      ? JSON.parse(config.data)
      : config.data
    : {}

  await wait()

  let data: ApiResponse<unknown>
  let status = 200

  if (url.endsWith('/auth/sms/send') && method === 'POST') {
    data = ok({ expireIn: 300, resendAfter: 60 })
  } else if (url.endsWith('/auth/login/sms') && method === 'POST') {
    if (payload.code !== MOCK_CODE) {
      data = fail(41004, '验证码错误，请重新输入')
    } else {
      mockUser = {
        id: 1,
        name: payload.role === 'WORKER' ? '王阿姨' : '林小满',
        phone: payload.phone,
        role: payload.role === 'WORKER' ? 'WORKER' : 'CUSTOMER',
        avatar: null,
      }
      const result: LoginResult = {
        token: `mock-token-${Date.now()}`,
        expiresIn: 7200,
        user: mockUser,
      }
      data = ok(result)
    }
  } else if (url.endsWith('/auth/me') && method === 'GET') {
    const current = mockUser ?? getStoredUser()
    data = current ? ok(current) : fail(40100, '未登录或登录已过期')
  } else if (url.endsWith('/auth/logout') && method === 'POST') {
    mockUser = null
    data = ok(null)
  } else {
    data = fail(40400, 'Mock 未实现该接口')
    status = 404
  }

  const response = {
    data,
    status,
    statusText: status === 200 ? 'OK' : 'ERROR',
    headers: {},
    config,
  } as AxiosResponse

  if (status >= 400) {
    throw new AxiosError(
      (data as ApiResponse<null>).message,
      'ERR_MOCK',
      config,
      undefined,
      response,
    )
  }
  return response
}

export const mockEnabled =
  import.meta.env.DEV && import.meta.env.VITE_USE_MOCK !== 'false'

if (mockEnabled) {
  http.defaults.adapter = adapter
  console.info('[mock] 登录验证码固定为 123456')
}
