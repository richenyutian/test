export interface MetricItem {
  name: string
  value: number
  extra: string
}

export interface ReportOverview {
  ticketTrend: MetricItem[]
  statusStatistics: MetricItem[]
  priorityStatistics: MetricItem[]
  handleGroupStatistics: MetricItem[]
  assigneeStatistics: MetricItem[]
  slaStatistics: MetricItem[]
  timeoutStatistics: MetricItem[]
}
