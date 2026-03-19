export interface SlaRuleItem {
  slaRuleId: number
  priorityCode: string
  responseLimitMinutes: number
  resolveLimitMinutes: number
  enabledFlag: number
  remark?: string
}

export interface SlaAlertItem {
  ticketId: number
  ticketNo: string
  title: string
  alertType: string
  ownerName: string
  teamName: string
  remainingTime: string
}

export interface SlaOverview {
  rules: SlaRuleItem[]
  alerts: SlaAlertItem[]
}

export interface SlaRuleSaveRequest {
  priorityCode: string
  responseLimitMinutes: number
  resolveLimitMinutes: number
  enabledFlag: number
  remark?: string
}

export interface SlaScanResult {
  responseTimeoutCount: number
  resolveTimeoutCount: number
  noticeCount: number
}
