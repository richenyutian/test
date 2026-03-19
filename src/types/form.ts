export type FieldType =
  | 'input'
  | 'textarea'
  | 'select'
  | 'remoteSelect'
  | 'number'
  | 'date'
  | 'switch'
  | 'richtext'

export interface FieldOption {
  label: string
  value: string
}

export interface FieldValidation {
  message?: string
  min?: number
  max?: number
  pattern?: string
  trigger?: 'blur' | 'change'
}

export interface RemoteSelectConfig {
  url: string
  method: 'GET' | 'POST'
  labelKey: string
  valueKey: string
  keywordKey: string
  resultPath: string
}

export interface FormField {
  id: string
  label: string
  prop: string
  type: FieldType
  placeholder: string
  required: boolean
  defaultValue: string | number | boolean | null
  helpText: string
  span: number
  options: FieldOption[]
  remoteConfig?: RemoteSelectConfig
  validation: FieldValidation
}

export interface FormLayoutConfig {
  columns: 1 | 2 | 3
  labelWidth: number
  labelPosition: 'left' | 'right' | 'top'
  size: 'large' | 'default' | 'small'
  gutter: number
}

export interface FormTemplate {
  id: string
  name: string
  code: string
  description: string
  category: string
  status: 'draft' | 'published'
  updatedAt: string
  layout: FormLayoutConfig
  fields: FormField[]
}

export interface FormSubmission {
  id: string
  formId: string
  formName: string
  submittedAt: string
  data: Record<string, any>
}
