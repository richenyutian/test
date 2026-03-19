export const ticketStatusLabelMap: Record<string, string> = {
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

export const ticketStatusTagTypeMap: Record<string, 'success' | 'warning' | 'info' | 'danger' | 'primary'> = {
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

export const sourceLabelMap: Record<string, string> = {
  USER_SUBMIT: '用户提交',
  SERVICE_ENTRY: '客服录入',
}

export const ticketTypeLabelMap: Record<string, string> = {
  INCIDENT: '故障',
  REQUEST: '需求',
  CHANGE: '变更',
  CONSULT: '咨询',
}

export const categoryLabelMap: Record<string, string> = {
  ACCOUNT: '账号权限',
  NETWORK: '网络链路',
  DATABASE: '数据库',
  APPLICATION: '应用系统',
}

export const priorityLabelMap: Record<string, string> = {
  P1: 'P1',
  P2: 'P2',
  P3: 'P3',
  P4: 'P4',
}

export const dataScopeLabelMap: Record<string, string> = {
  SELF_CREATED: '仅本人创建',
  SELF_ASSIGNED: '仅本人处理',
  DEPARTMENT: '本部门',
  TEAM: '本组',
  ALL: '全部',
}
