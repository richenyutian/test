<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import { computed, onMounted, ref } from 'vue'

import { getDashboardOverviewApi } from '../../api/modules/dashboard'
import EChartPanel from '../../components/chart/EChartPanel.vue'
import type { DashboardOverview } from '../../types/dashboard'

const loading = ref(false)
const overview = ref<DashboardOverview>()

const trendOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: overview.value?.ticketTrend.map((item) => item.date) ?? [],
  },
  yAxis: { type: 'value' },
  series: [
    {
      type: 'line',
      smooth: true,
      areaStyle: {},
      data: overview.value?.ticketTrend.map((item) => item.count) ?? [],
    },
  ],
}))

const statusOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      data: overview.value?.statusDistribution.map((item) => ({
        name: item.label,
        value: Number(item.value),
      })) ?? [],
    },
  ],
}))

const loadData = async () => {
  loading.value = true
  try {
    overview.value = await getDashboardOverviewApi()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="dashboard-cards">
      <el-card
        v-for="item in overview?.metrics || []"
        :key="item.label"
        class="data-card"
      >
        <div class="data-card__label">{{ item.label }}</div>
        <div class="data-card__value">{{ item.value }}</div>
        <div class="data-card__trend">{{ item.trend }}</div>
      </el-card>
    </div>

    <div class="grid-two">
      <el-card class="module-card">
        <template #header>
          <div class="module-card__title">工单趋势</div>
        </template>
        <EChartPanel :option="trendOption" height="340px" />
      </el-card>

      <el-card class="module-card">
        <template #header>
          <div class="module-card__title">状态分布</div>
        </template>
        <EChartPanel :option="statusOption" height="340px" />
      </el-card>
    </div>

    <div class="grid-two">
      <el-card class="module-card">
        <template #header>
          <div class="module-card__title">状态统计卡片</div>
        </template>

        <div class="record-list">
          <div
            v-for="item in overview?.statusDistribution || []"
            :key="item.label"
            class="record-card"
          >
            <div class="record-title">{{ item.label }}</div>
            <div class="record-time">{{ item.value }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="module-card">
        <template #header>
          <div class="module-card__title">个人待办</div>
        </template>

        <div class="record-list">
          <div
            v-for="item in overview?.personalTasks || []"
            :key="item.label"
            class="record-card"
          >
            <div class="record-title">{{ item.label }}</div>
            <div class="record-time">{{ item.value }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>
