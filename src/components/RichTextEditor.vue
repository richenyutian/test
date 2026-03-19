<script setup lang="ts">
import { computed, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: string
    placeholder?: string
    disabled?: boolean
  }>(),
  {
    placeholder: '请输入内容',
    disabled: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
  blur: []
}>()

const editorRef = ref<HTMLDivElement>()
const innerHtml = ref(props.modelValue || '')

watch(
  () => props.modelValue,
  (value) => {
    if (value !== innerHtml.value) {
      innerHtml.value = value || ''

      if (editorRef.value && editorRef.value.innerHTML !== innerHtml.value) {
        editorRef.value.innerHTML = innerHtml.value
      }
    }
  },
)

const isEmpty = computed(() => !extractText(innerHtml.value))

const syncValue = () => {
  const value = editorRef.value?.innerHTML || ''
  innerHtml.value = value
  emit('update:modelValue', value)
}

const exec = (command: string, promptText?: string) => {
  if (props.disabled) {
    return
  }

  if (promptText) {
    const url = window.prompt(promptText, 'https://')

    if (!url) {
      return
    }

    document.execCommand(command, false, url)
  } else {
    document.execCommand(command)
  }

  syncValue()
  editorRef.value?.focus()
}

const handleInput = () => {
  syncValue()
}

const handleBlur = () => {
  syncValue()
  emit('blur')
}

const actions = [
  { label: 'B', title: '加粗', command: 'bold' },
  { label: 'I', title: '斜体', command: 'italic' },
  { label: 'U', title: '下划线', command: 'underline' },
  { label: '• 列表', title: '无序列表', command: 'insertUnorderedList' },
  { label: '1. 列表', title: '有序列表', command: 'insertOrderedList' },
  { label: '链接', title: '插入链接', command: 'createLink', prompt: '请输入链接地址' },
  { label: '清除格式', title: '清除格式', command: 'removeFormat' },
]

const extractText = (value: string) =>
  value
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .trim()
</script>

<template>
  <div class="rich-editor" :class="{ disabled }">
    <div class="rich-editor__toolbar">
      <el-button
        v-for="action in actions"
        :key="action.command"
        text
        size="small"
        :disabled="disabled"
        @click="exec(action.command, action.prompt)"
      >
        {{ action.label }}
      </el-button>
    </div>

    <div class="rich-editor__body">
      <div
        ref="editorRef"
        class="rich-editor__content"
        :class="{ 'is-empty': isEmpty }"
        :contenteditable="!disabled"
        :data-placeholder="placeholder"
        @input="handleInput"
        @blur="handleBlur"
        v-html="innerHtml"
      />
    </div>
  </div>
</template>
