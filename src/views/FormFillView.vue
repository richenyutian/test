<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

import FormRenderer from '../components/FormRenderer.vue'
import { useFormStore } from '../stores/formStore'
import type { FormTemplate } from '../types/form'
import { createFormModel, createFormRules, formatTime } from '../utils/form'

type RendererExpose = {
  validate: () => Promise<boolean>
  resetFields: () => void
}

const store = useFormStore()

const activeTemplateId = ref(store.publishedTemplates[0]?.id || '')
const rendererRef = ref<RendererExpose>()
const submitting = ref(false)
const formModel = ref<Record<string, any>>({})

const activeTemplate = computed<FormTemplate | undefined>(() =>
  store.publishedTemplates.find((item) => item.id === activeTemplateId.value),
)

const formRules = computed(() =>
  activeTemplate.value ? createFormRules(activeTemplate.value) : {},
)

watch(
  activeTemplate,
  (template) => {
    formModel.value = template ? createFormModel(template) : {}
  },
  { immediate: true },
)

const handleSubmit = async () => {
  if (!activeTemplate.value) {
    ElMessage.warning('请先选择一个已发布的表单')
    return
  }

  submitting.value = true

  try {
    const valid = await rendererRef.value?.validate()

    if (!valid) {
      return
    }

    store.submitForm(activeTemplate.value.id, formModel.value)
    formModel.value = createFormModel(activeTemplate.value)
    ElMessage.success('表单提交成功')
  } catch {
    ElMessage.warning('请先完成必填项并修正校验错误')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page-stack">
    <div class="fill-layout">
      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">选择表单</div>
              <div class="panel-desc">仅展示已发布模板，点击后即可填写并提交</div>
            </div>
          </div>
        </template>

        <div v-if="store.publishedTemplates.length" class="template-list">
          <div
            v-for="template in store.publishedTemplates"
            :key="template.id"
            class="template-card"
            :class="{ active: activeTemplateId === template.id }"
            @click="activeTemplateId = template.id"
          >
            <div class="template-card__header">
              <div class="template-card__name">{{ template.name }}</div>
              <el-tag type="success">可填写</el-tag>
            </div>

            <div class="template-card__meta chip-row">
              <el-tag effect="plain">{{ template.category }}</el-tag>
              <el-tag effect="plain">字段 {{ template.fields.length }}</el-tag>
              <el-tag effect="plain">
                提交 {{ store.submissionCountByFormId[template.id] || 0 }}
              </el-tag>
            </div>

            <div class="template-card__desc">
              {{ template.description || '暂无模板说明' }}
            </div>
          </div>
        </div>

        <el-empty v-else description="还没有可填写的已发布表单，请先到管理页发布模板" />
      </el-card>

      <div class="page-stack">
        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <div>
                <div class="panel-title">
                  {{ activeTemplate?.name || '请选择左侧表单模板' }}
                </div>
                <div class="panel-desc">
                  {{ activeTemplate?.description || '选择模板后即可看到动态渲染的表单内容' }}
                </div>
              </div>
            </div>
          </template>

          <template v-if="activeTemplate">
            <div class="chip-row" style="margin-bottom: 18px">
              <el-tag>{{ activeTemplate.category }}</el-tag>
              <el-tag type="success">
                已提交 {{ store.submissionCountByFormId[activeTemplate.id] || 0 }} 次
              </el-tag>
              <el-tag effect="plain">
                最近更新 {{ formatTime(activeTemplate.updatedAt) }}
              </el-tag>
            </div>

            <FormRenderer
              ref="rendererRef"
              :template="activeTemplate"
              :model="formModel"
              :rules="formRules"
              :loading="submitting"
              show-submit
              submit-text="提交表单"
              @submit="handleSubmit"
            />
          </template>

          <el-empty v-else description="请选择一个模板开始填写" />
        </el-card>

        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <div>
                <div class="panel-title">最近提交记录</div>
                <div class="panel-desc">便于验证填写流程和查看提交结果</div>
              </div>
            </div>
          </template>

          <div v-if="store.latestSubmissions.length" class="record-list">
            <div
              v-for="record in store.latestSubmissions"
              :key="record.id"
              class="record-card"
            >
              <div class="record-title">{{ record.formName }}</div>
              <div class="record-time">{{ formatTime(record.submittedAt) }}</div>
            </div>
          </div>

          <el-empty v-else description="还没有提交记录，提交一次表单后将显示在这里" />
        </el-card>
      </div>
    </div>
  </div>
</template>
