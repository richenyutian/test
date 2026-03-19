import request from '../request'
import type { LoginRequest, LoginResponse, UserProfile } from '../../types/auth'

export const loginApi = (payload: LoginRequest) =>
  request.post<unknown, LoginResponse>('/v1/auth/login', payload)

export const profileApi = () =>
  request.get<unknown, UserProfile>('/v1/auth/profile')

export const logoutApi = () =>
  request.post<unknown, null>('/v1/auth/logout')
