import request from '../request'
import type { PageResponse, OptionItem } from '../../types/api'
import type { UserPageItem, UserSaveRequest } from '../../types/system'

export const getUserPageApi = (params: { current: number; size: number; keyword?: string }) =>
  request.get<unknown, PageResponse<UserPageItem>>('/v1/system/users', { params })

export const createUserApi = (payload: UserSaveRequest) =>
  request.post<unknown, number>('/v1/system/users', payload)

export const updateUserApi = (userId: number, payload: UserSaveRequest) =>
  request.put<unknown, null>(`/v1/system/users/${userId}`, payload)

export const getUserOptionsApi = () =>
  request.get<unknown, OptionItem[]>('/v1/system/users/options')
