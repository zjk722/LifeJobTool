import axios, { AxiosError } from 'axios'
import type { AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types/auth'
import { getToken } from '@/utils/auth-storage'

export class ApiError extends Error {
  constructor(
    public readonly code: number,
    message: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const body = error.response?.data
    if (body?.message) {
      return Promise.reject(new ApiError(body.code ?? -1, body.message))
    }
    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new ApiError(-1, '请求超时，请重试'))
    }
    return Promise.reject(new ApiError(-1, '网络异常，请稍后再试'))
  },
)

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await http.request<ApiResponse<T>>(config)
  const body = response.data
  if (!body || body.code !== 0) {
    throw new ApiError(body?.code ?? -1, body?.message ?? '请求失败')
  }
  return body.data
}
