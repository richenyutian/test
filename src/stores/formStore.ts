import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { defaultTemplates } from '../mock/defaultForms'
import type { FormSubmission, FormTemplate } from '../types/form'

const TEMPLATE_KEY = 'form-edit-studio/templates'
const SUBMISSION_KEY = 'form-edit-studio/submissions'

const clone = <T>(value: T): T => JSON.parse(JSON.stringify(value)) as T

const loadStorage = <T>(key: string, fallback: T) => {
  if (typeof window === 'undefined') {
    return clone(fallback)
  }

  const value = window.localStorage.getItem(key)

  if (!value) {
    return clone(fallback)
  }

  try {
    return JSON.parse(value) as T
  } catch {
    return clone(fallback)
  }
}

export const useFormStore = defineStore('form-store', () => {
  const templates = ref<FormTemplate[]>(
    loadStorage<FormTemplate[]>(TEMPLATE_KEY, defaultTemplates),
  )
  const submissions = ref<FormSubmission[]>(
    loadStorage<FormSubmission[]>(SUBMISSION_KEY, []),
  )

  const persistTemplates = () => {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(TEMPLATE_KEY, JSON.stringify(templates.value))
    }
  }

  const persistSubmissions = () => {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(SUBMISSION_KEY, JSON.stringify(submissions.value))
    }
  }

  const templateOptions = computed(() =>
    templates.value.map((item) => ({
      label: item.name,
      value: item.id,
      status: item.status,
    })),
  )

  const publishedTemplates = computed(() =>
    templates.value.filter((item) => item.status === 'published'),
  )

  const submissionCountByFormId = computed<Record<string, number>>(() =>
    submissions.value.reduce<Record<string, number>>((map, item) => {
      map[item.formId] = (map[item.formId] || 0) + 1

      return map
    }, {}),
  )

  const latestSubmissions = computed(() =>
    [...submissions.value]
      .sort((a, b) => +new Date(b.submittedAt) - +new Date(a.submittedAt))
      .slice(0, 10),
  )

  const getTemplateById = (id: string) =>
    templates.value.find((item) => item.id === id)

  const upsertTemplate = (template: FormTemplate) => {
    const target = {
      ...clone(template),
      updatedAt: new Date().toISOString(),
    }
    const index = templates.value.findIndex((item) => item.id === target.id)

    if (index >= 0) {
      templates.value[index] = target
    } else {
      templates.value.unshift(target)
    }

    persistTemplates()
  }

  const removeTemplate = (id: string) => {
    templates.value = templates.value.filter((item) => item.id !== id)
    submissions.value = submissions.value.filter((item) => item.formId !== id)
    persistTemplates()
    persistSubmissions()
  }

  const submitForm = (formId: string, data: Record<string, any>) => {
    const template = getTemplateById(formId)

    if (!template) {
      return
    }

    submissions.value.unshift({
      id: crypto.randomUUID(),
      formId,
      formName: template.name,
      submittedAt: new Date().toISOString(),
      data: clone(data),
    })
    persistSubmissions()
  }

  return {
    templates,
    submissions,
    templateOptions,
    publishedTemplates,
    submissionCountByFormId,
    latestSubmissions,
    getTemplateById,
    upsertTemplate,
    removeTemplate,
    submitForm,
  }
})
