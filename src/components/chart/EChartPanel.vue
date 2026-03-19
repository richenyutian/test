<script setup lang="ts">
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps<{
  option: echarts.EChartsOption
  height?: string
}>()

const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

const renderChart = async () => {
  await nextTick()

  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  chartInstance.setOption(props.option, true)
  chartInstance.resize()
}

watch(() => props.option, renderChart, { deep: true })

onMounted(() => {
  renderChart()
  window.addEventListener('resize', renderChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', renderChart)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<template>
  <div ref="chartRef" :style="{ width: '100%', height: height || '320px' }" />
</template>
