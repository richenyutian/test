export interface ApiResponse<T> {
  code: string
  message: string
  data: T
}

export interface PageResponse<T> {
  current: number
  size: number
  total: number
  records: T[]
}

export interface OptionItem {
  value: number
  label: string
}
