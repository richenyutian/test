import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { loginApi, logoutApi, profileApi } from '../api/modules/auth'
import { tokenStorage } from '../api/request'
import type { LoginResponse, UserProfile } from '../types/auth'

const PROFILE_KEY = 'work-order-profile'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(tokenStorage.get() || '')
  const profile = ref<UserProfile | null>(loadProfile())

  const isLoggedIn = computed(() => Boolean(token.value))

  const login = async (username: string) => {
    const response = await loginApi({ username })
    applyLogin(response)
    return response
  }

  const fetchProfile = async () => {
    const data = await profileApi()
    profile.value = data
    persistProfile()
    return data
  }

  const logout = async () => {
    try {
      if (token.value) {
        await logoutApi()
      }
    } finally {
      clearLogin()
    }
  }

  const hasMenuPermission = (permission?: string) => {
    if (!permission) return true
    return profile.value?.menuPermissions.includes(permission) ?? false
  }

  const hasButtonPermission = (permission?: string) => {
    if (!permission) return true
    const buttonPermissions = profile.value?.buttonPermissions ?? []
    return (
      buttonPermissions.includes(permission) ||
      buttonPermissions.includes('ticket:*') ||
      buttonPermissions.includes('system:*')
    )
  }

  const applyLogin = (data: LoginResponse) => {
    token.value = data.accessToken
    profile.value = data.profile
    tokenStorage.set(data.accessToken)
    persistProfile()
  }

  const clearLogin = () => {
    token.value = ''
    profile.value = null
    tokenStorage.clear()
    window.localStorage.removeItem(PROFILE_KEY)
  }

  const persistProfile = () => {
    if (profile.value) {
      window.localStorage.setItem(PROFILE_KEY, JSON.stringify(profile.value))
    }
  }

  return {
    token,
    profile,
    isLoggedIn,
    login,
    fetchProfile,
    logout,
    hasMenuPermission,
    hasButtonPermission,
    clearLogin,
  }
})

function loadProfile() {
  const raw = window.localStorage.getItem(PROFILE_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw) as UserProfile
  } catch {
    return null
  }
}
