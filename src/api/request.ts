import axios from 'axios'
import { ElMessage } from 'element-plus'

import type { ApiResponse } from '../types/api'

const TOKEN_KEY = 'work-order-token'

const request = axios.create({
  baseURL: '/api',
  timeout: 20000,
})

request.interceptors.request.use((config) => {
  const token = window.localStorage.getItem(TOKEN_KEY)

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse<unknown>

    if (data && typeof data.code === 'string') {
      if (data.code !== 'SUCCESS') {
        ElMessage.error(data.message || '请求失败')
        return Promise.reject(new Error(data.message || '请求失败'))
      }

      return data.data
    }

    return response.data
  },
  (error) => {
    const message = error?.response?.data?.message || error.message || '网络异常'

    if (error?.response?.status === 401) {
      window.localStorage.removeItem(TOKEN_KEY)
      if (window.location.hash !== '#/login') {
        window.location.hash = '/login'
      }
    }

    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export const tokenStorage = {
  get() {
    return window.localStorage.getItem(TOKEN_KEY)
  },
  set(token: string) {
    window.localStorage.setItem(TOKEN_KEY, token)
  },
  clear() {
    window.localStorage.removeItem(TOKEN_KEY)
  },
}

export default request
