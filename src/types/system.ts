import type { OptionItem } from './api'

export interface UserPageItem {
  userId: number
  username: string
  displayName: string
  userType: string
  externalUserFlag: number
  phone?: string
  email?: string
  status: string
  remark?: string
  roles: string[]
  primaryHandleGroupName?: string
  createdAt: string
}

export interface UserSaveRequest {
  username: string
  displayName: string
  userType: string
  externalUserFlag: number
  phone?: string
  email?: string
  status: string
  remark?: string
  roleIds: number[]
  primaryHandleGroupId?: number
}

export interface RoleItem {
  roleId: number
  roleCode: string
  roleName: string
  dataScope: string
  status: string
  remark?: string
  menuIds: number[]
}

export interface RoleSaveRequest {
  roleCode: string
  roleName: string
  dataScope: string
  status: string
  remark?: string
  menuIds: number[]
}

export interface MenuItem {
  menuId: number
  parentId: number
  menuName: string
  menuType: string
  routePath?: string
  componentPath?: string
  permissionCode?: string
  icon?: string
  sortOrder: number
  visibleFlag: number
  enabledFlag: number
  keepAliveFlag: number
  remark?: string
  children: MenuItem[]
}

export interface MenuSaveRequest {
  parentId: number
  menuName: string
  menuType: string
  routePath?: string
  componentPath?: string
  permissionCode?: string
  icon?: string
  sortOrder: number
  visibleFlag: number
  enabledFlag: number
  keepAliveFlag: number
  remark?: string
}

export interface HandleGroupItem {
  handleGroupId: number
  groupCode: string
  groupName: string
  groupType: string
  leaderUserId?: number
  leaderName?: string
  status: string
  remark?: string
}

export interface HandleGroupSaveRequest {
  groupCode: string
  groupName: string
  groupType: string
  leaderUserId?: number
  leaderName?: string
  status: string
  remark?: string
}

export type SelectOption = OptionItem
