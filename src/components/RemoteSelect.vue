<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'

import type { RemoteSelectConfig } from '../types/form'

interface RemoteOption {
  label: string
  value: string | number
  raw: Record<string, any>
}

const props = withDefaults(
  defineProps<{
    modelValue: string | number | null
    config: RemoteSelectConfig
    placeholder?: string
    disabled?: boolean
  }>(),
  {
    modelValue: '',
    placeholder: '请选择',
    disabled: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string | number | null]
}>()

const loading = ref(false)
const options = ref<RemoteOption[]>([])
const errorMessage = ref('')

const hasConfig = computed(
  () =>
    Boolean(props.config.url) &&
    Boolean(props.config.labelKey) &&
    Boolean(props.config.valueKey),
)

watch(
  () => props.config,
  () => {
    void loadOptions()
  },
  { deep: true },
)

onMounted(() => {
  void loadOptions()
})

const loadOptions = async (keyword = '') => {
  if (!hasConfig.value) {
    options.value = []
    errorMessage.value = '请先配置远程接口'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const response = await fetch(buildRequestUrl(keyword), buildRequestInit(keyword))

    if (!response.ok) {
      throw new Error(`接口请求失败：${response.status}`)
    }

    const data = (await response.json()) as Record<string, any>
    const list = resolveDataList(data, props.config.resultPath)

    if (!Array.isArray(list)) {
      throw new Error('接口返回结果不是数组')
    }

    const mapped = list
      .map((item) => ({
        label: String(resolveValue(item, props.config.labelKey) ?? ''),
        value: (resolveValue(item, props.config.valueKey) ?? '') as string | number,
        raw: item,
      }))
      .filter((item) => item.label && item.value !== '')

    options.value = keyword
      ? mapped.filter((item) => item.label.toLowerCase().includes(keyword.toLowerCase()))
      : mapped
  } catch (error) {
    options.value = []
    errorMessage.value =
      error instanceof Error ? error.message : '远程数据加载失败'
  } finally {
    loading.value = false
  }
}

const buildRequestUrl = (keyword: string) => {
  if (props.config.method === 'POST') {
    return props.config.url
  }

  const url = new URL(props.config.url, window.location.origin)

  if (keyword && props.config.keywordKey) {
    url.searchParams.set(props.config.keywordKey, keyword)
  }

  if (props.config.url.startsWith('http://') || props.config.url.startsWith('https://')) {
    return url.toString()
  }

  return `${url.pathname}${url.search}`
}

const buildRequestInit = (keyword: string) => {
  if (props.config.method !== 'POST') {
    return undefined
  }

  const body = props.config.keywordKey
    ? JSON.stringify({
        [props.config.keywordKey]: keyword,
      })
    : undefined

  return {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body,
  }
}

const resolveDataList = (payload: Record<string, any>, path: string) => {
  if (!path) {
    return payload
  }

  return path.split('.').reduce<any>((result, segment) => result?.[segment], payload)
}

const resolveValue = (payload: Record<string, any>, path: string) =>
  path.split('.').reduce<any>((result, segment) => result?.[segment], payload)
</script>

<template>
  <el-select
    :model-value="modelValue"
    :disabled="disabled"
    :loading="loading"
    :placeholder="placeholder"
    filterable
    remote
    clearable
    style="width: 100%"
    :remote-method="loadOptions"
    @update:model-value="emit('update:modelValue', $event)"
    @visible-change="(visible) => visible && loadOptions()"
  >
    <el-option
      v-for="option in options"
      :key="String(option.value)"
      :label="option.label"
      :value="option.value"
    />

    <template v-if="errorMessage" #empty>
      <div class="remote-select__empty">
        {{ errorMessage }}
      </div>
    </template>
  </el-select>
</template>
