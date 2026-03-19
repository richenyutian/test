import request from '../request'
import type { ReportOverview } from '../../types/report'

export const getReportOverviewApi = () =>
  request.get<unknown, ReportOverview>('/v1/reports/overview')
