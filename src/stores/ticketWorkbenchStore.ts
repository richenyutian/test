import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import {
  mockAuditLogs,
  mockLifecycleRules,
  mockNotifications,
  mockRolePermissions,
  mockSlaRules,
  mockTickets,
  mockUsers,
} from '../mock/ticketSystemMock'
import type { TicketRecord, TicketStatus } from '../types/ticketSystem'

const clone = <T>(value: T): T => JSON.parse(JSON.stringify(value)) as T

export const useTicketWorkbenchStore = defineStore('ticket-workbench-store', () => {
  const tickets = ref<TicketRecord[]>(clone(mockTickets))
  const notifications = ref(clone(mockNotifications))
  const auditLogs = ref(clone(mockAuditLogs))
  const lifecycleRules = ref(clone(mockLifecycleRules))
  const slaRules = ref(clone(mockSlaRules))
  const rolePermissions = ref(clone(mockRolePermissions))
  const users = ref(clone(mockUsers))

  const dashboardMetrics = computed(() => [
    { label: '工单总量', value: tickets.value.length, trend: '+12%' },
    { label: '处理中工单', value: countByStatus('PROCESSING'), trend: '+5%' },
    { label: '超时工单', value: tickets.value.filter((item) => item.timeout).length, trend: '-2%' },
    { label: 'SLA 达成率', value: '91.4%', trend: '+1.6%' },
  ])

  const ticketTrend = computed(() => {
    return Array.from({ length: 7 }, (_, index) => {
      const date = new Date()
      date.setDate(date.getDate() - (6 - index))
      const dateKey = date.toISOString().slice(0, 10)
      const count = tickets.value.filter((ticket) => ticket.createdAt.slice(0, 10) === dateKey).length

      return {
        date: dateKey,
        count,
      }
    })
  })

  const statusStatistics = computed(() =>
    aggregate(tickets.value, (ticket) => ticket.status),
  )

  const categoryStatistics = computed(() =>
    aggregate(tickets.value, (ticket) => ticket.categoryName),
  )

  const assigneeStatistics = computed(() =>
    aggregate(tickets.value, (ticket) => ticket.currentAssigneeName),
  )

  const departmentStatistics = computed(() =>
    aggregate(tickets.value, (ticket) => ticket.departmentName),
  )

  const satisfactionStatistics = computed(() => {
    const completed = tickets.value.filter((ticket) => ticket.satisfactionLevel)
    return aggregate(completed, (ticket) => ticket.satisfactionLevel || '未评价')
  })

  const pendingDispatchTickets = computed(() =>
    tickets.value.filter((ticket) =>
      ticket.status === 'PENDING_ASSIGN' || ticket.status === 'REOPENED',
    ),
  )

  const slaAlerts = computed(() =>
    tickets.value
      .filter((ticket) => ticket.timeout || ticket.escalated || ticket.status === 'PENDING_ACCEPT')
      .slice(0, 8)
      .map((ticket) => ({
        ticketNo: ticket.ticketNo,
        title: ticket.title,
        alertType: ticket.timeout
          ? '已超时'
          : ticket.status === 'PENDING_ACCEPT'
            ? '响应预警'
            : '升级预警',
        ownerName: ticket.currentAssigneeName,
        teamName: ticket.currentGroupName,
        remainingTime: ticket.timeout ? '已超时' : '30 分钟',
      })),
  )

  const unreadNotificationCount = computed(
    () => notifications.value.filter((item) => !item.read).length,
  )

  const getTicketById = (ticketId: string) =>
    tickets.value.find((ticket) => ticket.ticketId === ticketId)

  const createTicket = (payload: {
    title: string
    description: string
    requesterName: string
    contactPhone: string
    customerName: string
    departmentName: string
    ticketType: TicketRecord['ticketType']
    categoryCode: string
    categoryName: string
    priority: TicketRecord['priority']
  }) => {
    const ticketId = `TID-${tickets.value.length + 1}`
    const ticketNo = `WO${new Date().toISOString().slice(0, 10).replaceAll('-', '')}${String(tickets.value.length + 1).padStart(4, '0')}`
    const createdAt = new Date().toISOString()

    tickets.value.unshift({
      ticketId,
      ticketNo,
      title: payload.title,
      description: payload.description,
      requesterId: 'U1001',
      requesterName: payload.requesterName,
      contactPhone: payload.contactPhone,
      customerName: payload.customerName,
      departmentName: payload.departmentName,
      source: 'MANUAL_SUBMISSION',
      ticketType: payload.ticketType,
      categoryCode: payload.categoryCode,
      categoryName: payload.categoryName,
      priority: payload.priority,
      urgencyLevel: payload.priority === 'P1' ? 'CRITICAL' : payload.priority === 'P2' ? 'HIGH' : 'MEDIUM',
      status: 'PENDING_ACCEPT',
      currentAssigneeId: '',
      currentAssigneeName: '待分派',
      currentGroupCode: 'CS-DESK',
      currentGroupName: '客服台',
      responseDeadline: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
      resolveDeadline: new Date(Date.now() + 8 * 60 * 60 * 1000).toISOString(),
      createdAt,
      updatedAt: createdAt,
      tags: ['新建工单'],
      escalated: false,
      timeout: false,
      flowRecords: [
        {
          actionType: 'CREATE',
          operatorName: payload.requesterName,
          fromStatus: '',
          toStatus: 'PENDING_ACCEPT',
          remark: '提单人创建工单，等待客服受理。',
          operatedAt: createdAt,
        },
      ],
      comments: [],
    })
  }

  return {
    tickets,
    notifications,
    auditLogs,
    lifecycleRules,
    slaRules,
    rolePermissions,
    users,
    dashboardMetrics,
    ticketTrend,
    statusStatistics,
    categoryStatistics,
    assigneeStatistics,
    departmentStatistics,
    satisfactionStatistics,
    pendingDispatchTickets,
    slaAlerts,
    unreadNotificationCount,
    getTicketById,
    createTicket,
  }

  function countByStatus(status: TicketStatus) {
    return tickets.value.filter((item) => item.status === status).length
  }
})

function aggregate<T>(source: T[], selector: (item: T) => string) {
  const map = new Map<string, number>()

  for (const item of source) {
    const key = selector(item)
    map.set(key, (map.get(key) || 0) + 1)
  }

  return Array.from(map.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
}
