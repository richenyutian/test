<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

import RemoteSelect from './RemoteSelect.vue'
import RichTextEditor from './RichTextEditor.vue'
import type { FormTemplate } from '../types/form'
import { getFieldSpan } from '../utils/form'

const emptyRemoteConfig = {
  url: '',
  method: 'GET',
  labelKey: 'label',
  valueKey: 'value',
  keywordKey: 'keyword',
  resultPath: '',
} as const

const props = withDefaults(
  defineProps<{
    template: FormTemplate
    model: Record<string, any>
    rules?: FormRules
    disabled?: boolean
    showSubmit?: boolean
    submitText?: string
    loading?: boolean
  }>(),
  {
    disabled: false,
    showSubmit: false,
    submitText: '提交',
    loading: false,
  },
)

const emit = defineEmits<{
  submit: []
}>()

const formRef = ref<FormInstance>()

const validate = () => formRef.value?.validate()
const resetFields = () => formRef.value?.resetFields()
const validateField = (prop: string) => formRef.value?.validateField(prop)

defineExpose({
  validate,
  resetFields,
})
</script>

<template>
  <el-form
    ref="formRef"
    :model="model"
    :rules="rules"
    :label-width="`${template.layout.labelWidth}px`"
    :label-position="template.layout.labelPosition"
    :size="template.layout.size"
    status-icon
  >
    <el-row :gutter="template.layout.gutter">
      <el-col
        v-for="field in template.fields"
        :key="field.id"
        :span="getFieldSpan(field, template)"
      >
        <el-form-item :label="field.label" :prop="field.prop">
          <template v-if="field.type === 'input'">
            <el-input
              v-model="model[field.prop]"
              :disabled="disabled"
              :placeholder="field.placeholder"
              clearable
            />
          </template>

          <template v-else-if="field.type === 'textarea'">
            <el-input
              v-model="model[field.prop]"
              type="textarea"
              :disabled="disabled"
              :placeholder="field.placeholder"
              :rows="4"
            />
          </template>

          <template v-else-if="field.type === 'select'">
            <el-select
              v-model="model[field.prop]"
              :disabled="disabled"
              :placeholder="field.placeholder"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="option in field.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </template>

          <template v-else-if="field.type === 'remoteSelect'">
            <RemoteSelect
              v-model="model[field.prop]"
              :config="field.remoteConfig || emptyRemoteConfig"
              :disabled="disabled"
              :placeholder="field.placeholder"
              @update:model-value="validateField(field.prop)"
            />
          </template>

          <template v-else-if="field.type === 'number'">
            <el-input-number
              v-model="model[field.prop]"
              :disabled="disabled"
              :placeholder="field.placeholder"
              controls-position="right"
              style="width: 100%"
            />
          </template>

          <template v-else-if="field.type === 'date'">
            <el-date-picker
              v-model="model[field.prop]"
              type="date"
              value-format="YYYY-MM-DD"
              :disabled="disabled"
              :placeholder="field.placeholder || '请选择日期'"
              style="width: 100%"
            />
          </template>

          <template v-else-if="field.type === 'switch'">
            <el-switch
              v-model="model[field.prop]"
              :disabled="disabled"
              inline-prompt
              active-text="是"
              inactive-text="否"
            />
          </template>

          <template v-else-if="field.type === 'richtext'">
            <RichTextEditor
              v-model="model[field.prop]"
              :disabled="disabled"
              :placeholder="field.placeholder || '请输入富文本内容'"
              @blur="validateField(field.prop)"
            />
          </template>

          <div v-if="field.helpText" class="field-help">
            {{ field.helpText }}
          </div>
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item v-if="showSubmit">
      <el-button
        type="primary"
        :loading="loading"
        @click="emit('submit')"
      >
        {{ submitText }}
      </el-button>
      <el-button @click="resetFields()">重置</el-button>
    </el-form-item>
  </el-form>
</template>
