<script setup lang="ts">
import { computed } from 'vue'

import { ticketStatusLabelMap, ticketStatusTagTypeMap } from '../../utils/dict'

const props = defineProps<{
  status: string
}>()

const label = computed(() => ticketStatusLabelMap[props.status] || props.status)
const type = computed(() => ticketStatusTagTypeMap[props.status] || 'info')
</script>

<template>
  <el-tag :type="type">{{ label }}</el-tag>
</template>
