import type { FormRules } from 'element-plus'

import type { FieldType, FormField, FormTemplate } from '../types/form'

export const formatTime = (value: string) =>
  new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))

export const getDefaultValueByType = (type: FieldType) => {
  switch (type) {
    case 'number':
      return null
    case 'switch':
      return false
    default:
      return ''
  }
}

export const createEmptyField = (): FormField => ({
  id: crypto.randomUUID(),
  label: '',
  prop: '',
  type: 'input',
  placeholder: '',
  required: true,
  defaultValue: '',
  helpText: '',
  span: 24,
  options: [],
  validation: {
    message: '',
    min: undefined,
    max: undefined,
    pattern: '',
    trigger: 'blur',
  },
})

export const createEmptyTemplate = (): FormTemplate => ({
  id: crypto.randomUUID(),
  name: '',
  code: '',
  description: '',
  category: '默认分类',
  status: 'draft',
  updatedAt: new Date().toISOString(),
  layout: {
    columns: 2,
    labelWidth: 110,
    labelPosition: 'top',
    size: 'default',
    gutter: 16,
  },
  fields: [],
})

export const createFormModel = (template: FormTemplate) =>
  template.fields.reduce<Record<string, any>>((model, field) => {
    model[field.prop] =
      field.defaultValue ?? getDefaultValueByType(field.type)

    return model
  }, {})

export const createFormRules = (template: FormTemplate): FormRules =>
  template.fields.reduce<FormRules>((rules, field) => {
    const fieldRules: Array<Record<string, unknown>> = []

    if (field.required) {
      fieldRules.push({
        required: true,
        message: field.validation.message || `请填写${field.label}`,
        trigger: field.type === 'select' || field.type === 'date' || field.type === 'switch'
          ? 'change'
          : field.validation.trigger || 'blur',
      })
    }

    if (typeof field.validation.min === 'number' || typeof field.validation.max === 'number') {
      fieldRules.push({
        min: field.validation.min,
        max: field.validation.max,
        message:
          field.validation.message ||
          buildLengthMessage(field),
        trigger: field.validation.trigger || 'blur',
      })
    }

    if (field.validation.pattern) {
      try {
        fieldRules.push({
          pattern: new RegExp(field.validation.pattern),
          message: field.validation.message || `${field.label}格式不正确`,
          trigger: field.validation.trigger || 'blur',
        })
      } catch {
        // 忽略非法正则，避免设计中的临时输入影响整页渲染。
      }
    }

    if (fieldRules.length) {
      rules[field.prop] = fieldRules
    }

    return rules
  }, {})

export const getFieldSpan = (field: FormField, template: FormTemplate) =>
  field.span || Math.floor(24 / template.layout.columns)

const buildLengthMessage = (field: FormField) => {
  if (
    typeof field.validation.min === 'number' &&
    typeof field.validation.max === 'number'
  ) {
    return `${field.label}长度需在 ${field.validation.min} - ${field.validation.max} 之间`
  }

  if (typeof field.validation.min === 'number') {
    return `${field.label}长度不能少于 ${field.validation.min}`
  }

  if (typeof field.validation.max === 'number') {
    return `${field.label}长度不能超过 ${field.validation.max}`
  }

  return `请检查${field.label}`
}
