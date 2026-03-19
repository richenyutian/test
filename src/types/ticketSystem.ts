export type RoleCode =
  | 'REQUESTER'
  | 'CUSTOMER_SERVICE'
  | 'TECHNICIAN'
  | 'SUPERVISOR'
  | 'ADMIN'

export type DataScope =
  | 'SELF_CREATED'
  | 'SELF_ASSIGNED'
  | 'DEPARTMENT'
  | 'TEAM'
  | 'ALL'

export type TicketStatus =
  | 'PENDING_ACCEPT'
  | 'ACCEPTED'
  | 'PENDING_ASSIGN'
  | 'PROCESSING'
  | 'PENDING_CONFIRM'
  | 'COMPLETED'
  | 'CLOSED'
  | 'CANCELLED'
  | 'SUSPENDED'
  | 'REOPENED'

export interface UserProfile {
  userId: string
  username: string
  password: string
  displayName: string
  departmentName: string
  teamName: string
  roles: RoleCode[]
  menuPermissions: string[]
  buttonPermissions: string[]
  dataScope: DataScope
}

export interface TicketActionRecord {
  actionType: string
  operatorName: string
  fromStatus: TicketStatus | ''
  toStatus: TicketStatus | ''
  remark: string
  operatedAt: string
}

export interface TicketCommentRecord {
  commentId: string
  authorName: string
  authorRole: RoleCode
  content: string
  createdAt: string
}

export interface TicketRecord {
  ticketId: string
  ticketNo: string
  title: string
  description: string
  requesterId: string
  requesterName: string
  contactPhone: string
  customerName: string
  departmentName: string
  source: 'MANUAL_SUBMISSION' | 'SERVICE_DESK_ENTRY'
  ticketType: '咨询' | '故障' | '需求' | '变更'
  categoryCode: string
  categoryName: string
  priority: 'P1' | 'P2' | 'P3' | 'P4'
  urgencyLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  status: TicketStatus
  currentAssigneeId: string
  currentAssigneeName: string
  currentGroupCode: string
  currentGroupName: string
  responseDeadline: string
  resolveDeadline: string
  createdAt: string
  updatedAt: string
  acceptedAt?: string
  assignedAt?: string
  completedAt?: string
  closedAt?: string
  tags: string[]
  escalated: boolean
  timeout: boolean
  satisfactionLevel?: '满意' | '一般' | '不满意'
  satisfactionComment?: string
  flowRecords: TicketActionRecord[]
  comments: TicketCommentRecord[]
}

export interface SlaRule {
  ruleCode: string
  ruleName: string
  categoryName: string
  responseMinutes: number
  resolveMinutes: number
  responseWarningMinutes: number
  resolveWarningMinutes: number
  autoEscalate: boolean
}

export interface NotificationMessage {
  messageId: string
  title: string
  channel: 'INTERNAL' | 'EMAIL'
  receiverName: string
  content: string
  read: boolean
  sentAt: string
}

export interface AuditLogRecord {
  logId: string
  moduleName: string
  operationName: string
  operatorName: string
  requestPath: string
  result: string
  operatedAt: string
}

export interface RolePermissionMatrix {
  roleCode: RoleCode
  roleName: string
  menuPermissions: string[]
  buttonPermissions: string[]
  dataScope: DataScope
}

export interface LifecycleRule {
  currentStatus: TicketStatus
  actionName: string
  targetStatus: TicketStatus
  allowedRoles: RoleCode[]
}
