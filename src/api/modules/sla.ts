import request from '../request'
import type { SlaOverview, SlaRuleSaveRequest, SlaScanResult } from '../../types/sla'

export const getSlaOverviewApi = () =>
  request.get<unknown, SlaOverview>('/v1/sla/overview')

export const createSlaRuleApi = (payload: SlaRuleSaveRequest) =>
  request.post<unknown, number>('/v1/sla/rules', payload)

export const updateSlaRuleApi = (slaRuleId: number, payload: SlaRuleSaveRequest) =>
  request.put<unknown, null>(`/v1/sla/rules/${slaRuleId}`, payload)

export const scanSlaApi = () =>
  request.post<unknown, SlaScanResult>('/v1/sla/scan')
