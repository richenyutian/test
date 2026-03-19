import type {
  AuditLogRecord,
  LifecycleRule,
  NotificationMessage,
  RolePermissionMatrix,
  SlaRule,
  TicketRecord,
  TicketStatus,
  UserProfile,
} from '../types/ticketSystem'

const now = new Date()

const categories = [
  { code: 'ACCOUNT', name: '账号权限' },
  { code: 'SYSTEM_ERROR', name: '系统故障' },
  { code: 'API_ERROR', name: '接口异常' },
  { code: 'PERFORMANCE', name: '性能问题' },
  { code: 'REPORT', name: '报表问题' },
  { code: 'DEPLOY', name: '环境部署' },
]

const teams = [
  { code: 'CS-DESK', name: '客服台' },
  { code: 'TS-APP', name: '应用支持组' },
  { code: 'TS-OPS', name: '运维保障组' },
  { code: 'RD-COOP', name: '研发协同组' },
]

const assignees = [
  { id: 'U3001', name: '陈工', teamCode: 'TS-APP', teamName: '应用支持组' },
  { id: 'U3002', name: '周工', teamCode: 'TS-OPS', teamName: '运维保障组' },
  { id: 'U3003', name: '吴工', teamCode: 'RD-COOP', teamName: '研发协同组' },
  { id: 'U3004', name: '何工', teamCode: 'TS-APP', teamName: '应用支持组' },
]

const departments = ['客户成功部', '财务部', '供应链部', '营销中心', '信息化部']

const statusCycle: TicketStatus[] = [
  'PENDING_ACCEPT',
  'ACCEPTED',
  'PENDING_ASSIGN',
  'PROCESSING',
  'PENDING_CONFIRM',
  'COMPLETED',
  'CLOSED',
  'CANCELLED',
  'SUSPENDED',
  'REOPENED',
]

export const mockUsers: UserProfile[] = [
  {
    userId: 'U1001',
    username: 'requester.lee',
    password: 'ChangeMe123!',
    displayName: '李晓明',
    departmentName: '客户成功部',
    teamName: '客户一组',
    roles: ['REQUESTER'],
    menuPermissions: ['dashboard:view', 'ticket:list', 'ticket:detail', 'notification:center'],
    buttonPermissions: ['ticket:create', 'ticket:urge', 'ticket:confirm', 'ticket:rate'],
    dataScope: 'SELF_CREATED',
  },
  {
    userId: 'U2001',
    username: 'service.desk',
    password: 'ChangeMe123!',
    displayName: '王客服',
    departmentName: '服务运营部',
    teamName: '客服台',
    roles: ['CUSTOMER_SERVICE'],
    menuPermissions: ['dashboard:view', 'ticket:list', 'ticket:detail', 'dispatch:center', 'notification:center'],
    buttonPermissions: ['ticket:accept', 'ticket:assign', 'ticket:cancel', 'ticket:close'],
    dataScope: 'TEAM',
  },
  {
    userId: 'U3001',
    username: 'tech.chen',
    password: 'ChangeMe123!',
    displayName: '陈工',
    departmentName: '技术支持部',
    teamName: '应用支持组',
    roles: ['TECHNICIAN'],
    menuPermissions: ['dashboard:view', 'ticket:list', 'ticket:detail', 'notification:center'],
    buttonPermissions: ['ticket:process', 'ticket:transfer', 'ticket:suspend', 'ticket:complete'],
    dataScope: 'SELF_ASSIGNED',
  },
  {
    userId: 'U4001',
    username: 'supervisor.zhao',
    password: 'ChangeMe123!',
    displayName: '赵主管',
    departmentName: '技术支持部',
    teamName: '应用支持组',
    roles: ['SUPERVISOR'],
    menuPermissions: ['dashboard:view', 'ticket:list', 'ticket:detail', 'dispatch:center', 'sla:manage', 'report:center', 'notification:center'],
    buttonPermissions: ['ticket:assign', 'ticket:escalate', 'ticket:resume', 'ticket:intervene'],
    dataScope: 'TEAM',
  },
  {
    userId: 'U9001',
    username: 'admin.root',
    password: 'ChangeMe123!',
    displayName: '系统管理员',
    departmentName: '信息化部',
    teamName: '平台管理组',
    roles: ['ADMIN'],
    menuPermissions: ['dashboard:view', 'ticket:list', 'ticket:detail', 'dispatch:center', 'sla:manage', 'report:center', 'system:config', 'audit:log', 'notification:center'],
    buttonPermissions: ['ticket:*', 'system:*', 'audit:view'],
    dataScope: 'ALL',
  },
]

export const mockLifecycleRules: LifecycleRule[] = [
  { currentStatus: 'PENDING_ACCEPT', actionName: '受理', targetStatus: 'ACCEPTED', allowedRoles: ['CUSTOMER_SERVICE'] },
  { currentStatus: 'ACCEPTED', actionName: '进入待分派', targetStatus: 'PENDING_ASSIGN', allowedRoles: ['CUSTOMER_SERVICE'] },
  { currentStatus: 'PENDING_ASSIGN', actionName: '正式分派', targetStatus: 'PROCESSING', allowedRoles: ['CUSTOMER_SERVICE', 'SUPERVISOR'] },
  { currentStatus: 'PROCESSING', actionName: '挂起', targetStatus: 'SUSPENDED', allowedRoles: ['TECHNICIAN', 'SUPERVISOR'] },
  { currentStatus: 'SUSPENDED', actionName: '恢复处理', targetStatus: 'PROCESSING', allowedRoles: ['TECHNICIAN', 'SUPERVISOR'] },
  { currentStatus: 'PROCESSING', actionName: '提交解决方案', targetStatus: 'PENDING_CONFIRM', allowedRoles: ['TECHNICIAN'] },
  { currentStatus: 'PENDING_CONFIRM', actionName: '确认解决', targetStatus: 'COMPLETED', allowedRoles: ['REQUESTER'] },
  { currentStatus: 'PENDING_CONFIRM', actionName: '确认未解决', targetStatus: 'REOPENED', allowedRoles: ['REQUESTER'] },
  { currentStatus: 'COMPLETED', actionName: '关闭', targetStatus: 'CLOSED', allowedRoles: ['CUSTOMER_SERVICE', 'ADMIN'] },
]

export const mockSlaRules: SlaRule[] = [
  {
    ruleCode: 'SLA-INC-P1',
    ruleName: 'P1 故障响应规则',
    categoryName: '系统故障',
    responseMinutes: 15,
    resolveMinutes: 120,
    responseWarningMinutes: 5,
    resolveWarningMinutes: 20,
    autoEscalate: true,
  },
  {
    ruleCode: 'SLA-INC-P2',
    ruleName: 'P2 接口异常规则',
    categoryName: '接口异常',
    responseMinutes: 30,
    resolveMinutes: 240,
    responseWarningMinutes: 10,
    resolveWarningMinutes: 30,
    autoEscalate: true,
  },
  {
    ruleCode: 'SLA-SRV-P3',
    ruleName: '一般服务请求规则',
    categoryName: '账号权限',
    responseMinutes: 60,
    resolveMinutes: 480,
    responseWarningMinutes: 20,
    resolveWarningMinutes: 60,
    autoEscalate: false,
  },
]

export const mockRolePermissions: RolePermissionMatrix[] = [
  {
    roleCode: 'REQUESTER',
    roleName: '提单人',
    menuPermissions: ['dashboard:view', 'ticket:list', 'ticket:detail', 'notification:center'],
    buttonPermissions: ['ticket:create', 'ticket:urge', 'ticket:confirm', 'ticket:rate'],
    dataScope: 'SELF_CREATED',
  },
  {
    roleCode: 'CUSTOMER_SERVICE',
    roleName: '客服',
    menuPermissions: ['dashboard:view', 'ticket:list', 'ticket:detail', 'dispatch:center', 'notification:center'],
    buttonPermissions: ['ticket:accept', 'ticket:assign', 'ticket:cancel', 'ticket:close'],
    dataScope: 'TEAM',
  },
  {
    roleCode: 'TECHNICIAN',
    roleName: '处理人员',
    menuPermissions: ['dashboard:view', 'ticket:list', 'ticket:detail', 'notification:center'],
    buttonPermissions: ['ticket:process', 'ticket:transfer', 'ticket:suspend', 'ticket:complete'],
    dataScope: 'SELF_ASSIGNED',
  },
  {
    roleCode: 'SUPERVISOR',
    roleName: '主管',
    menuPermissions: ['dashboard:view', 'ticket:list', 'ticket:detail', 'dispatch:center', 'sla:manage', 'report:center', 'notification:center'],
    buttonPermissions: ['ticket:assign', 'ticket:escalate', 'ticket:resume', 'ticket:intervene'],
    dataScope: 'TEAM',
  },
  {
    roleCode: 'ADMIN',
    roleName: '管理员',
    menuPermissions: ['dashboard:view', 'ticket:list', 'ticket:detail', 'dispatch:center', 'sla:manage', 'report:center', 'system:config', 'audit:log', 'notification:center'],
    buttonPermissions: ['ticket:*', 'system:*', 'audit:view'],
    dataScope: 'ALL',
  },
]

export const mockNotifications: NotificationMessage[] = [
  {
    messageId: 'MSG-1',
    title: '新工单待受理',
    channel: 'INTERNAL',
    receiverName: '王客服',
    content: '工单 WO202603190012 进入待受理队列，请及时处理。',
    read: false,
    sentAt: shiftTime(-20),
  },
  {
    messageId: 'MSG-2',
    title: 'SLA 即将超时',
    channel: 'INTERNAL',
    receiverName: '陈工',
    content: '工单 WO202603190018 距离解决超时仅剩 45 分钟。',
    read: false,
    sentAt: shiftTime(-45),
  },
  {
    messageId: 'MSG-3',
    title: '工单待确认',
    channel: 'EMAIL',
    receiverName: '李晓明',
    content: '您的工单已进入待确认状态，请确认处理结果。',
    read: true,
    sentAt: shiftTime(-180),
  },
]

export const mockAuditLogs: AuditLogRecord[] = [
  {
    logId: 'AUD-1',
    moduleName: '工单管理',
    operationName: '创建工单',
    operatorName: '李晓明',
    requestPath: '/api/v1/tickets',
    result: 'SUCCESS',
    operatedAt: shiftTime(-480),
  },
  {
    logId: 'AUD-2',
    moduleName: '分派中心',
    operationName: '分派工单',
    operatorName: '王客服',
    requestPath: '/api/v1/dispatch/assign',
    result: 'SUCCESS',
    operatedAt: shiftTime(-360),
  },
  {
    logId: 'AUD-3',
    moduleName: 'SLA 管理',
    operationName: '升级工单',
    operatorName: '赵主管',
    requestPath: '/api/v1/sla/escalate',
    result: 'SUCCESS',
    operatedAt: shiftTime(-120),
  },
  {
    logId: 'AUD-4',
    moduleName: '系统配置',
    operationName: '修改角色权限',
    operatorName: '系统管理员',
    requestPath: '/api/v1/system/roles',
    result: 'SUCCESS',
    operatedAt: shiftTime(-60),
  },
]

export const mockTickets: TicketRecord[] = Array.from({ length: 80 }, (_, index) => {
  const seed = index + 1
  const status = statusCycle[seed % statusCycle.length]
  const category = categories[seed % categories.length]
  const assignee = assignees[seed % assignees.length]
  const createdDate = new Date(now.getTime() - seed * 1000 * 60 * 60 * 6)
  const acceptedDate = new Date(createdDate.getTime() + 20 * 60 * 1000)
  const assignedDate = new Date(createdDate.getTime() + 40 * 60 * 1000)
  const completedDate = new Date(createdDate.getTime() + 4 * 60 * 60 * 1000)
  const closedDate = new Date(createdDate.getTime() + 5 * 60 * 60 * 1000)
  const responseDeadline = new Date(createdDate.getTime() + 2 * 60 * 60 * 1000)
  const resolveDeadline = new Date(createdDate.getTime() + 8 * 60 * 60 * 1000)

  return {
    ticketId: `TID-${seed}`,
    ticketNo: `WO20260319${String(seed).padStart(4, '0')}`,
    title: `核心业务工单 #${seed}`,
    description: '用于验证受理、分派、处理、SLA、评价、统计报表与审计链路的模拟工单。',
    requesterId: 'U1001',
    requesterName: `提交人${(seed % 8) + 1}`,
    contactPhone: `1380000${String(seed).padStart(4, '0')}`,
    customerName: ['华东大客户中心', '集团财务中心', '供应链业务中心', '营销中心', '总部信息化部'][seed % 5],
    departmentName: departments[seed % departments.length],
    source: seed % 2 === 0 ? 'MANUAL_SUBMISSION' : 'SERVICE_DESK_ENTRY',
    ticketType: ['咨询', '故障', '需求', '变更'][seed % 4] as TicketRecord['ticketType'],
    categoryCode: category.code,
    categoryName: category.name,
    priority: (['P1', 'P2', 'P3', 'P4'][seed % 4]) as TicketRecord['priority'],
    urgencyLevel: (['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'][seed % 4]) as TicketRecord['urgencyLevel'],
    status,
    currentAssigneeId: assignee.id,
    currentAssigneeName: assignee.name,
    currentGroupCode: assignee.teamCode,
    currentGroupName: assignee.teamName,
    responseDeadline: responseDeadline.toISOString(),
    resolveDeadline: resolveDeadline.toISOString(),
    createdAt: createdDate.toISOString(),
    updatedAt: new Date(createdDate.getTime() + 2 * 60 * 60 * 1000).toISOString(),
    acceptedAt: status === 'PENDING_ACCEPT' ? undefined : acceptedDate.toISOString(),
    assignedAt: ['PENDING_ASSIGN', 'PENDING_ACCEPT', 'ACCEPTED'].includes(status) ? undefined : assignedDate.toISOString(),
    completedAt: ['PENDING_CONFIRM', 'COMPLETED', 'CLOSED'].includes(status) ? completedDate.toISOString() : undefined,
    closedAt: status === 'CLOSED' ? closedDate.toISOString() : undefined,
    tags: ['VIP客户', '核心业务', '自动升级', '需回访'].slice(0, (seed % 3) + 1),
    escalated: seed % 6 === 0,
    timeout: seed % 7 === 0 || status === 'SUSPENDED',
    satisfactionLevel: ['COMPLETED', 'CLOSED'].includes(status)
      ? (['满意', '一般', '不满意'][seed % 3] as TicketRecord['satisfactionLevel'])
      : undefined,
    satisfactionComment: ['COMPLETED', 'CLOSED'].includes(status) ? '整体处理较为及时，建议优化前期沟通。' : undefined,
    flowRecords: buildFlowRecords(status, createdDate, acceptedDate, assignedDate, completedDate, assignee.name),
    comments: buildComments(seed, createdDate, assignee.name),
  }
})

function buildFlowRecords(
  status: TicketStatus,
  createdDate: Date,
  acceptedDate: Date,
  assignedDate: Date,
  completedDate: Date,
  assigneeName: string,
) {
  const records = [
    {
      actionType: 'CREATE',
      operatorName: '提交人',
      fromStatus: '' as const,
      toStatus: 'PENDING_ACCEPT' as TicketStatus,
      remark: '用户提交工单，系统生成工单编号并通知客服。',
      operatedAt: createdDate.toISOString(),
    },
  ]

  if (status !== 'PENDING_ACCEPT') {
    records.push({
      actionType: 'ACCEPT',
      operatorName: '王客服',
      fromStatus: 'PENDING_ACCEPT' as TicketStatus,
      toStatus: 'ACCEPTED' as TicketStatus,
      remark: '客服完成工单受理与有效性确认。',
      operatedAt: acceptedDate.toISOString(),
    })
  }

  if (!['PENDING_ACCEPT', 'ACCEPTED', 'PENDING_ASSIGN'].includes(status)) {
    records.push({
      actionType: 'ASSIGN',
      operatorName: '王客服',
      fromStatus: 'PENDING_ASSIGN' as TicketStatus,
      toStatus: 'PROCESSING' as TicketStatus,
      remark: `分派至 ${assigneeName} 处理。`,
      operatedAt: assignedDate.toISOString(),
    })
  }

  if (['PENDING_CONFIRM', 'COMPLETED', 'CLOSED'].includes(status)) {
    records.push({
      actionType: 'SUBMIT_SOLUTION',
      operatorName: assigneeName,
      fromStatus: 'PROCESSING' as TicketStatus,
      toStatus: 'PENDING_CONFIRM' as TicketStatus,
      remark: '处理人提交解决方案，等待用户确认。',
      operatedAt: completedDate.toISOString(),
    })
  }

  return records
}

function buildComments(seed: number, createdDate: Date, assigneeName: string) {
  return [
    {
      commentId: `CMT-${seed}-1`,
      authorName: '提交人',
      authorRole: 'REQUESTER' as const,
      content: '问题在高峰期复现明显，请优先排查。',
      createdAt: new Date(createdDate.getTime() + 5 * 60 * 1000).toISOString(),
    },
    {
      commentId: `CMT-${seed}-2`,
      authorName: '王客服',
      authorRole: 'CUSTOMER_SERVICE' as const,
      content: '已完成受理，正在转交技术处理。',
      createdAt: new Date(createdDate.getTime() + 30 * 60 * 1000).toISOString(),
    },
    {
      commentId: `CMT-${seed}-3`,
      authorName: assigneeName,
      authorRole: 'TECHNICIAN' as const,
      content: '初步定位为接口超时与缓存命中率下降导致。',
      createdAt: new Date(createdDate.getTime() + 90 * 60 * 1000).toISOString(),
    },
  ]
}

function shiftTime(minutes: number) {
  return new Date(now.getTime() + minutes * 60 * 1000).toISOString()
}
