import request from '../request'
import type { DashboardOverview } from '../../types/dashboard'

export const getDashboardOverviewApi = () =>
  request.get<unknown, DashboardOverview>('/v1/dashboard/overview')
