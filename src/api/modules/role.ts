import request from '../request'
import type { OptionItem } from '../../types/api'
import type { RoleItem, RoleSaveRequest } from '../../types/system'

export const getRoleListApi = () =>
  request.get<unknown, RoleItem[]>('/v1/system/roles')

export const createRoleApi = (payload: RoleSaveRequest) =>
  request.post<unknown, number>('/v1/system/roles', payload)

export const updateRoleApi = (roleId: number, payload: RoleSaveRequest) =>
  request.put<unknown, null>(`/v1/system/roles/${roleId}`, payload)

export const getRoleOptionsApi = () =>
  request.get<unknown, OptionItem[]>('/v1/system/roles/options')
