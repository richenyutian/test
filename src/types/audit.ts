export interface AuditLogItem {
  auditLogId: number
  operatorUserId?: number
  moduleName: string
  operationType: string
  operatorName: string
  businessId?: number
  operationDescription: string
  requestPath?: string
  requestIp?: string
  createdAt: string
}

export interface AuditLogPage {
  current: number
  size: number
  total: number
  records: AuditLogItem[]
}
