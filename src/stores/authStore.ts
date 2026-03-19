import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { mockUsers } from '../mock/ticketSystemMock'
import type { UserProfile } from '../types/ticketSystem'

const STORAGE_KEY = 'ticket-system/current-user'

const clone = <T>(value: T): T => JSON.parse(JSON.stringify(value)) as T

export const useAuthStore = defineStore('auth-store', () => {
  const currentUser = ref<UserProfile | null>(loadUser())

  const isLoggedIn = computed(() => Boolean(currentUser.value))

  const visibleMenus = computed(() => currentUser.value?.menuPermissions || [])
  const buttonPermissions = computed(() => currentUser.value?.buttonPermissions || [])

  const login = (username: string, password: string) => {
    const target = mockUsers.find(
      (user) => user.username === username && user.password === password,
    )

    if (!target) {
      throw new Error('用户名或密码错误')
    }

    currentUser.value = clone(target)
    persist()
  }

  const quickLogin = (user: UserProfile) => {
    currentUser.value = clone(user)
    persist()
  }

  const logout = () => {
    currentUser.value = null
    if (typeof window !== 'undefined') {
      window.localStorage.removeItem(STORAGE_KEY)
    }
  }

  const hasMenuPermission = (permission: string) =>
    visibleMenus.value.includes(permission)

  const hasButtonPermission = (permission: string) =>
    buttonPermissions.value.includes(permission) || buttonPermissions.value.includes('ticket:*') || buttonPermissions.value.includes('system:*')

  function persist() {
    if (typeof window !== 'undefined' && currentUser.value) {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(currentUser.value))
    }
  }

  return {
    currentUser,
    isLoggedIn,
    visibleMenus,
    buttonPermissions,
    login,
    quickLogin,
    logout,
    hasMenuPermission,
    hasButtonPermission,
  }
})

function loadUser() {
  if (typeof window === 'undefined') {
    return null
  }

  const raw = window.localStorage.getItem(STORAGE_KEY)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as UserProfile
  } catch {
    return null
  }
}
