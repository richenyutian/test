<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import { computed, onMounted, ref } from 'vue'

import { getReportOverviewApi } from '../../api/modules/report'
import EChartPanel from '../../components/chart/EChartPanel.vue'
import type { ReportOverview } from '../../types/report'

const loading = ref(false)
const report = ref<ReportOverview>()

const trendOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: report.value?.ticketTrend.map((item) => item.name) || [] },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: report.value?.ticketTrend.map((item) => item.value) || [] }],
}))

const statusOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item' },
  series: [{ type: 'pie', radius: '65%', data: report.value?.statusStatistics.map((item) => ({ name: item.name, value: item.value })) || [] }],
}))

const priorityOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item' },
  series: [{ type: 'pie', radius: '65%', data: report.value?.priorityStatistics.map((item) => ({ name: item.name, value: item.value })) || [] }],
}))

const groupOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: report.value?.handleGroupStatistics.map((item) => item.name) || [] },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: report.value?.handleGroupStatistics.map((item) => item.value) || [] }],
}))

const loadData = async () => {
  loading.value = true
  try {
    report.value = await getReportOverviewApi()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="grid-two">
      <el-card class="module-card">
        <template #header><div class="module-card__title">工单数量趋势</div></template>
        <EChartPanel :option="trendOption" />
      </el-card>
      <el-card class="module-card">
        <template #header><div class="module-card__title">按状态统计</div></template>
        <EChartPanel :option="statusOption" />
      </el-card>
    </div>

    <div class="grid-two">
      <el-card class="module-card">
        <template #header><div class="module-card__title">按优先级统计</div></template>
        <EChartPanel :option="priorityOption" />
      </el-card>
      <el-card class="module-card">
        <template #header><div class="module-card__title">按处理组统计</div></template>
        <EChartPanel :option="groupOption" />
      </el-card>
    </div>

    <div class="grid-two">
      <el-card class="module-card">
        <template #header><div class="module-card__title">处理人统计</div></template>
        <el-table :data="report?.assigneeStatistics || []" border>
          <el-table-column prop="name" label="处理人" />
          <el-table-column prop="value" label="工单数" width="100" />
          <el-table-column prop="extra" label="说明" />
        </el-table>
      </el-card>

      <el-card class="module-card">
        <template #header><div class="module-card__title">SLA 与超时统计</div></template>
        <div class="record-list">
          <div v-for="item in report?.slaStatistics || []" :key="item.name" class="record-card">
            <div class="record-title">{{ item.name }}</div>
            <div class="record-time">{{ item.value }} {{ item.extra }}</div>
          </div>
          <div v-for="item in report?.timeoutStatistics || []" :key="`timeout-${item.name}`" class="record-card">
            <div class="record-title">{{ item.name }}</div>
            <div class="record-time">{{ item.value }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>
