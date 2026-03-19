export interface MetricCard {
  label: string
  value: string
  trend: string
}

export interface TrendPoint {
  date: string
  count: number
}

export interface DashboardOverview {
  metrics: MetricCard[]
  ticketTrend: TrendPoint[]
  statusDistribution: MetricCard[]
  personalTasks: MetricCard[]
}
