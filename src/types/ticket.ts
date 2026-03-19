export interface TicketListItem {
  ticketId: number
  ticketNo: string
  title: string
  requesterName: string
  sourceCode: string
  ticketTypeCode: string
  categoryCode: string
  priorityCode: string
  currentStatus: string
  currentHandlerUserId?: number
  currentHandlerName?: string
  currentHandleGroupId?: number
  currentHandleGroupName?: string
  escalatedFlag: boolean
  timeoutFlag: boolean
  createdAt: string
  updatedAt: string
}

export interface TicketFlowRecord {
  flowRecordId: number
  actionCode: string
  operatorName: string
  fromStatus?: string
  toStatus?: string
  operateDescription: string
  operateTime: string
}

export interface TicketAttachment {
  attachmentId: number
  originalFileName: string
  storageFileName: string
  fileExtension?: string
  fileSize: number
  uploaderName: string
  createdAt: string
}

export interface TicketDetail {
  ticketId: number
  ticketNo: string
  title: string
  description: string
  requesterUserId: number
  requesterName: string
  contactPhone: string
  externalUserFlag: number
  sourceCode: string
  ticketTypeCode: string
  categoryCode: string
  priorityCode: string
  currentStatus: string
  currentHandlerUserId?: number
  currentHandlerName?: string
  currentHandleGroupId?: number
  currentHandleGroupName?: string
  escalatedFlag: number
  escalateReason?: string
  escalateTime?: string
  timeoutFlag: number
  responseDeadline: string
  resolveDeadline: string
  createdAt: string
  acceptedAt?: string
  assignedAt?: string
  processedAt?: string
  userConfirmedAt?: string
  closedAt?: string
  suspendReason?: string
  closeReason?: string
  reopenReason?: string
  resolutionSummary?: string
  attachments: TicketAttachment[]
  flowRecords: TicketFlowRecord[]
  allowedActions: string[]
}

export interface TicketLifecycleRule {
  currentStatus: string
  actionName: string
  targetStatus: string
  allowedRoles: string[]
}

export interface TicketCreateRequest {
  title: string
  description: string
  contactPhone: string
  externalUserFlag: number
  sourceCode: string
  ticketTypeCode: string
  categoryCode: string
  priorityCode: string
}

export interface TicketAcceptRequest {
  priorityCode: string
  categoryCode: string
}

export interface TicketAssignRequest {
  handleGroupId: number
  handlerUserId?: number
  operateDescription?: string
}

export interface TicketTransferRequest {
  handleGroupId: number
  handlerUserId?: number
  operateDescription: string
}

export interface TicketSuspendRequest {
  suspendReason: string
}

export interface TicketCompleteRequest {
  resolutionSummary: string
}

export interface TicketCloseRequest {
  closeReason: string
}

export interface TicketReopenRequest {
  reopenReason: string
}

export interface TicketEscalateRequest {
  escalateReason: string
}
