import type { TicketStatus } from '../types/ticketSystem'

export const formatDateTime = (value?: string) => {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

export const statusLabelMap: Record<TicketStatus, string> = {
  PENDING_ACCEPT: '待受理',
  ACCEPTED: '已受理',
  PENDING_ASSIGN: '待分派',
  PROCESSING: '处理中',
  PENDING_CONFIRM: '待用户确认',
  COMPLETED: '已完成',
  CLOSED: '已关闭',
  CANCELLED: '已取消',
  SUSPENDED: '已挂起',
  REOPENED: '已重开',
}

export const statusTagTypeMap: Record<TicketStatus, 'info' | 'warning' | 'primary' | 'success' | 'danger'> = {
  PENDING_ACCEPT: 'warning',
  ACCEPTED: 'primary',
  PENDING_ASSIGN: 'warning',
  PROCESSING: 'primary',
  PENDING_CONFIRM: 'warning',
  COMPLETED: 'success',
  CLOSED: 'info',
  CANCELLED: 'info',
  SUSPENDED: 'danger',
  REOPENED: 'warning',
}

export const sourceLabelMap = {
  MANUAL_SUBMISSION: '用户手工提交',
  SERVICE_DESK_ENTRY: '客服代录入',
}
