import request from '../request'
import type { OptionItem } from '../../types/api'
import type { MenuItem, MenuSaveRequest } from '../../types/system'

export const getMenuTreeApi = () =>
  request.get<unknown, MenuItem[]>('/v1/system/menus/tree')

export const getMenuOptionsApi = () =>
  request.get<unknown, OptionItem[]>('/v1/system/menus/options')

export const createMenuApi = (payload: MenuSaveRequest) =>
  request.post<unknown, number>('/v1/system/menus', payload)

export const updateMenuApi = (menuId: number, payload: MenuSaveRequest) =>
  request.put<unknown, null>(`/v1/system/menus/${menuId}`, payload)
