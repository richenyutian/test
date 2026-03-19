export interface LoginRequest {
  username: string
}

export interface UserProfile {
  userId: number
  username: string
  displayName: string
  userType: string
  roles: string[]
  menuPermissions: string[]
  buttonPermissions: string[]
  dataScope: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  profile: UserProfile
}
