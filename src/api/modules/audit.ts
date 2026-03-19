import request from '../request'
import type { AuditLogPage } from '../../types/audit'

export const getAuditLogPageApi = (params: { current: number; size: number }) =>
  request.get<unknown, AuditLogPage>('/v1/audits', { params })
