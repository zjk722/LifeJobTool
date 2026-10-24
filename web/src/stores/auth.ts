import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'
import type { LoginPayload, UserInfo } from '@/types/auth'
import {
  clearStoredUser,
  clearToken,
  getStoredUser,
  getToken,
  setStoredUser,
  setToken,
} from '@/utils/auth-storage'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<UserInfo | null>(getStoredUser())
  const isLoggedIn = computed(() => Boolean(token.value))

  async function sendCode(phone: string): Promise<void> {
    await authApi.sendSmsCode(phone)
  }

  async function login(payload: LoginPayload) {
    const result = await authApi.loginBySms(payload)
    token.value = result.token
    user.value = result.user
    setToken(result.token)
    setStoredUser(result.user)
    return result
  }

  async function fetchMe(): Promise<UserInfo> {
    const me = await authApi.getMe()
    user.value = me
    setStoredUser(me)
    return me
  }

  async function logout(): Promise<void> {
    try {
      await authApi.logout()
    } catch {
      // 服务端登出失败不阻塞本地清理
    }
    clear()
  }

  function clear(): void {
    token.value = null
    user.value = null
    clearToken()
    clearStoredUser()
  }

  return { token, user, isLoggedIn, sendCode, login, fetchMe, logout, clear }
})
