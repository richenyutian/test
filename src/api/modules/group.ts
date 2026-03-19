import request from '../request'
import type { OptionItem } from '../../types/api'
import type { HandleGroupItem, HandleGroupSaveRequest } from '../../types/system'

export const getGroupListApi = () =>
  request.get<unknown, HandleGroupItem[]>('/v1/system/handle-groups')

export const createGroupApi = (payload: HandleGroupSaveRequest) =>
  request.post<unknown, number>('/v1/system/handle-groups', payload)

export const updateGroupApi = (groupId: number, payload: HandleGroupSaveRequest) =>
  request.put<unknown, null>(`/v1/system/handle-groups/${groupId}`, payload)

export const getGroupOptionsApi = () =>
  request.get<unknown, OptionItem[]>('/v1/system/handle-groups/options')
