<script setup lang="ts">
import {
  Delete,
  EditPen,
  Plus,
  Top,
  Bottom,
} from '@element-plus/icons-vue'
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

import FormRenderer from '../components/FormRenderer.vue'
import { useFormStore } from '../stores/formStore'
import type { FieldOption, FormField, FormTemplate } from '../types/form'
import {
  createEmptyField,
  createEmptyTemplate,
  createFormModel,
  createFormRules,
  formatTime,
  getDefaultValueByType,
} from '../utils/form'

const store = useFormStore()

const dialogVisible = ref(false)
const fieldDialogVisible = ref(false)
const activeTab = ref('basic')
const editingFieldIndex = ref(-1)

const templateFormRef = ref<FormInstance>()
const fieldFormRef = ref<FormInstance>()

const editingTemplate = reactive<FormTemplate>(createEmptyTemplate())
const fieldDraft = reactive<FormField>(createEmptyField())

const fieldTypeOptions = [
  { label: '单行输入框', value: 'input' },
  { label: '多行输入框', value: 'textarea' },
  { label: '下拉选择', value: 'select' },
  { label: '远程下拉', value: 'remoteSelect' },
  { label: '数字输入', value: 'number' },
  { label: '日期选择', value: 'date' },
  { label: '开关', value: 'switch' },
  { label: '富文本', value: 'richtext' },
]

const fieldTypeLabelMap = Object.fromEntries(
  fieldTypeOptions.map((item) => [item.value, item.label]),
)
const createDefaultRemoteConfig = () => ({
  url: '',
  method: 'GET' as const,
  labelKey: 'label',
  valueKey: 'value',
  keywordKey: 'keyword',
  resultPath: '',
})

const clone = <T,>(value: T): T => JSON.parse(JSON.stringify(value)) as T

const assignTemplate = (template: FormTemplate) => {
  Object.assign(editingTemplate, clone(template))
}

const assignField = (field: FormField) => {
  Object.assign(fieldDraft, clone(field))
}

const previewModel = computed(() => createFormModel(editingTemplate))
const previewRules = computed(() => createFormRules(editingTemplate))
const numberDefaultValue = computed({
  get: () =>
    typeof fieldDraft.defaultValue === 'number' ? fieldDraft.defaultValue : undefined,
  set: (value?: number) => {
    fieldDraft.defaultValue = value ?? null
  },
})
const switchDefaultValue = computed({
  get: () => Boolean(fieldDraft.defaultValue),
  set: (value: boolean) => {
    fieldDraft.defaultValue = value
  },
})

const isSelectField = computed(() => fieldDraft.type === 'select')
const isRemoteSelectField = computed(() => fieldDraft.type === 'remoteSelect')
const remoteConfigDraft = computed(() => {
  if (!fieldDraft.remoteConfig) {
    fieldDraft.remoteConfig = createDefaultRemoteConfig()
  }

  return fieldDraft.remoteConfig
})

const stats = computed(() => [
  {
    label: '表单模板总数',
    value: store.templates.length,
  },
  {
    label: '已发布模板',
    value: store.publishedTemplates.length,
  },
  {
    label: '累计提交次数',
    value: store.submissions.length,
  },
])

const templateFormRules: FormRules = {
  name: [{ required: true, message: '请输入表单名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入表单编码', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
      message: '编码需以字母开头，仅支持字母、数字和下划线',
      trigger: 'blur',
    },
  ],
  category: [{ required: true, message: '请输入表单分类', trigger: 'blur' }],
}

const fieldFormRules: FormRules = {
  label: [{ required: true, message: '请输入字段名称', trigger: 'blur' }],
  prop: [
    { required: true, message: '请输入字段标识', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
      message: '字段标识需以字母开头，仅支持字母、数字和下划线',
      trigger: 'blur',
    },
  ],
  type: [{ required: true, message: '请选择字段类型', trigger: 'change' }],
}

const openCreateDialog = () => {
  assignTemplate(createEmptyTemplate())
  activeTab.value = 'basic'
  dialogVisible.value = true
}

const openEditDialog = (template: FormTemplate) => {
  assignTemplate(template)
  activeTab.value = 'basic'
  dialogVisible.value = true
}

const openCreateFieldDialog = () => {
  editingFieldIndex.value = -1
  assignField(createEmptyField())
  fieldDialogVisible.value = true
}

const openEditFieldDialog = (field: FormField, index: number) => {
  editingFieldIndex.value = index
  assignField(field)
  fieldDialogVisible.value = true
}

const handleFieldTypeChange = () => {
  fieldDraft.defaultValue = getDefaultValueByType(fieldDraft.type)

  if (fieldDraft.type !== 'select') {
    fieldDraft.options = []
  }

  if (fieldDraft.type !== 'remoteSelect') {
    fieldDraft.remoteConfig = createDefaultRemoteConfig()
  }

  if (fieldDraft.type === 'switch' || fieldDraft.type === 'select' || fieldDraft.type === 'date' || fieldDraft.type === 'remoteSelect') {
    fieldDraft.validation.trigger = 'change'
  } else {
    fieldDraft.validation.trigger = 'blur'
  }
}

const addOption = () => {
  fieldDraft.options.push({
    label: '',
    value: '',
  })
}

const removeOption = (index: number) => {
  fieldDraft.options.splice(index, 1)
}

const saveField = async () => {
  const valid = await fieldFormRef.value?.validate().catch(() => false)

  if (!valid) {
    return
  }

  const duplicatedProp = editingTemplate.fields.some((field, index) => {
    if (editingFieldIndex.value === index) {
      return false
    }

    return field.prop === fieldDraft.prop
  })

  if (duplicatedProp) {
    ElMessage.warning('字段标识不能重复')
    return
  }

  if (fieldDraft.type === 'select') {
    const validOptions = fieldDraft.options.filter(
      (option) => option.label.trim() && option.value.trim(),
    )

    if (!validOptions.length) {
      ElMessage.warning('下拉字段至少需要一个有效选项')
      return
    }

    fieldDraft.options = validOptions as FieldOption[]
  }

  if (fieldDraft.type === 'remoteSelect') {
    const remoteConfig = remoteConfigDraft.value

    if (
      !remoteConfig.url.trim() ||
      !remoteConfig.labelKey.trim() ||
      !remoteConfig.valueKey.trim()
    ) {
      ElMessage.warning('请完善远程下拉的接口地址和字段映射')
      return
    }
  }

  const nextField = clone(fieldDraft)

  if (editingFieldIndex.value >= 0) {
    editingTemplate.fields.splice(editingFieldIndex.value, 1, nextField)
  } else {
    editingTemplate.fields.push(nextField)
  }

  fieldDialogVisible.value = false
}

const moveField = (index: number, direction: 'up' | 'down') => {
  const targetIndex = direction === 'up' ? index - 1 : index + 1

  if (targetIndex < 0 || targetIndex >= editingTemplate.fields.length) {
    return
  }

  const fields = [...editingTemplate.fields]
  ;[fields[index], fields[targetIndex]] = [fields[targetIndex], fields[index]]
  editingTemplate.fields = fields
}

const removeField = (index: number) => {
  editingTemplate.fields.splice(index, 1)
}

const saveTemplate = async () => {
  const valid = await templateFormRef.value?.validate().catch(() => false)

  if (!valid) {
    activeTab.value = 'basic'
    return
  }

  if (!editingTemplate.fields.length) {
    activeTab.value = 'fields'
    ElMessage.warning('请至少配置一个字段')
    return
  }

  const duplicatedCode = store.templates.some(
    (template) =>
      template.code === editingTemplate.code && template.id !== editingTemplate.id,
  )

  if (duplicatedCode) {
    activeTab.value = 'basic'
    ElMessage.warning('表单编码不能重复')
    return
  }

  store.upsertTemplate(clone(editingTemplate))
  dialogVisible.value = false
  ElMessage.success('表单模板已保存')
}

const deleteTemplate = async (template: FormTemplate) => {
  try {
    await ElMessageBox.confirm(
      `删除后将同步移除“${template.name}”的历史提交记录，是否继续？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
      },
    )

    store.removeTemplate(template.id)
    ElMessage.success('表单模板已删除')
  } catch {
    // 用户取消删除时不提示错误。
  }
}
</script>

<template>
  <div class="page-stack">
    <div class="stats-grid">
      <el-card
        v-for="item in stats"
        :key="item.label"
        class="stat-card"
        shadow="hover"
      >
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ item.value }}</div>
      </el-card>
    </div>

    <div class="content-grid">
      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">表单模板列表</div>
              <div class="panel-desc">支持新增、编辑、删除和模板复用</div>
            </div>

            <el-button type="primary" :icon="Plus" @click="openCreateDialog">
              新建表单
            </el-button>
          </div>
        </template>

        <div class="template-list">
          <div
            v-for="template in store.templates"
            :key="template.id"
            class="template-card"
          >
            <div class="template-card__header">
              <div class="template-card__name">{{ template.name }}</div>
              <el-tag :type="template.status === 'published' ? 'success' : 'info'">
                {{ template.status === 'published' ? '已发布' : '草稿' }}
              </el-tag>
            </div>

            <div class="template-card__meta chip-row">
              <el-tag effect="plain">{{ template.category }}</el-tag>
              <el-tag effect="plain">字段 {{ template.fields.length }}</el-tag>
              <el-tag effect="plain">
                提交 {{ store.submissionCountByFormId[template.id] || 0 }}
              </el-tag>
              <el-tag effect="plain">编码 {{ template.code }}</el-tag>
            </div>

            <div class="template-card__desc">
              {{ template.description || '暂无模板说明，可进入编辑器完善设计信息。' }}
            </div>

            <div class="template-card__footer">
              <span class="muted-text">
                最近更新：{{ formatTime(template.updatedAt) }}
              </span>

              <div class="chip-row">
                <el-button text type="primary" :icon="EditPen" @click="openEditDialog(template)">
                  编辑
                </el-button>
                <el-button text type="danger" :icon="Delete" @click="deleteTemplate(template)">
                  删除
                </el-button>
              </div>
            </div>
          </div>

          <el-empty
            v-if="!store.templates.length"
            class="empty-state"
            description="还没有任何表单模板，点击右上角开始创建"
          />
        </div>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">最近提交记录</div>
              <div class="panel-desc">查看用户最近提交的表单数据快照</div>
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
            <el-scrollbar max-height="120px" style="margin-top: 10px">
              <pre class="muted-text">{{ JSON.stringify(record.data, null, 2) }}</pre>
            </el-scrollbar>
          </div>
        </div>

        <el-empty
          v-else
          class="empty-state"
          description="当前还没有提交记录，可以到“表单填写”页面体验"
        />
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingTemplate.name ? `编辑表单 - ${editingTemplate.name}` : '新建表单'"
      width="88%"
      top="4vh"
      destroy-on-close
    >
      <div class="content-grid">
        <el-card class="panel-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基础信息" name="basic">
              <el-form
                ref="templateFormRef"
                :model="editingTemplate"
                :rules="templateFormRules"
                label-position="top"
              >
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="表单名称" prop="name">
                      <el-input
                        v-model="editingTemplate.name"
                        placeholder="如：供应商准入申请表"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="表单编码" prop="code">
                      <el-input
                        v-model="editingTemplate.code"
                        placeholder="如：supplier_apply"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="表单分类" prop="category">
                      <el-input v-model="editingTemplate.category" placeholder="请输入分类" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="状态">
                      <el-segmented
                        v-model="editingTemplate.status"
                        :options="[
                          { label: '草稿', value: 'draft' },
                          { label: '发布', value: 'published' },
                        ]"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="模板说明">
                      <el-input
                        v-model="editingTemplate.description"
                        type="textarea"
                        :rows="4"
                        placeholder="描述该表单的业务场景、填写对象和审批说明"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="字段设计" name="fields">
              <div class="designer-toolbar">
                <div class="muted-text">
                  支持字段类型、默认值、提示文案、校验规则和显示宽度设计
                </div>
                <el-button type="primary" :icon="Plus" @click="openCreateFieldDialog">
                  新增字段
                </el-button>
              </div>

              <el-table :data="editingTemplate.fields" border>
                <el-table-column prop="label" label="字段名称" min-width="140" />
                <el-table-column prop="prop" label="字段标识" min-width="140" />
                <el-table-column label="类型" width="120">
                  <template #default="{ row }">
                    {{ fieldTypeLabelMap[row.type] || row.type }}
                  </template>
                </el-table-column>
                <el-table-column label="必填" width="90">
                  <template #default="{ row }">
                    <el-tag :type="row.required ? 'danger' : 'info'">
                      {{ row.required ? '必填' : '选填' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="布局宽度" width="100">
                  <template #default="{ row }">{{ row.span }}/24</template>
                </el-table-column>
                <el-table-column label="校验说明" min-width="220">
                  <template #default="{ row }">
                    {{ row.validation.message || '未单独配置提示语，使用默认校验文案' }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="{ row, $index }">
                    <div class="chip-row">
                      <el-button text type="primary" @click="openEditFieldDialog(row, $index)">
                        编辑
                      </el-button>
                      <el-button text @click="moveField($index, 'up')">
                        <el-icon><Top /></el-icon>
                      </el-button>
                      <el-button text @click="moveField($index, 'down')">
                        <el-icon><Bottom /></el-icon>
                      </el-button>
                      <el-button text type="danger" @click="removeField($index)">
                        删除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="布局配置" name="layout">
              <el-form :model="editingTemplate.layout" label-position="top">
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="默认列数">
                      <el-radio-group v-model="editingTemplate.layout.columns">
                        <el-radio-button :value="1">1 列</el-radio-button>
                        <el-radio-button :value="2">2 列</el-radio-button>
                        <el-radio-button :value="3">3 列</el-radio-button>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="标签位置">
                      <el-segmented
                        v-model="editingTemplate.layout.labelPosition"
                        :options="[
                          { label: '上方', value: 'top' },
                          { label: '左侧', value: 'left' },
                          { label: '右侧', value: 'right' },
                        ]"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="标签宽度">
                      <el-slider
                        v-model="editingTemplate.layout.labelWidth"
                        :min="80"
                        :max="180"
                        show-input
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="栅格间距">
                      <el-slider
                        v-model="editingTemplate.layout.gutter"
                        :min="0"
                        :max="24"
                        show-input
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="组件尺寸">
                      <el-segmented
                        v-model="editingTemplate.layout.size"
                        :options="[
                          { label: '大', value: 'large' },
                          { label: '默认', value: 'default' },
                          { label: '小', value: 'small' },
                        ]"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-tab-pane>
          </el-tabs>

          <template #footer />
        </el-card>

        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <div>
                <div class="panel-title">实时预览</div>
                <div class="panel-desc">当前模板设计效果与前端校验规则预览</div>
              </div>
            </div>
          </template>

          <div class="preview-wrapper">
            <FormRenderer
              :template="editingTemplate"
              :model="previewModel"
              :rules="previewRules"
              disabled
            />
          </div>
        </el-card>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveTemplate">保存模板</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="fieldDialogVisible"
      :title="editingFieldIndex >= 0 ? '编辑字段' : '新增字段'"
      width="760px"
      destroy-on-close
    >
      <el-form
        ref="fieldFormRef"
        :model="fieldDraft"
        :rules="fieldFormRules"
        label-position="top"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="字段名称" prop="label">
              <el-input v-model="fieldDraft.label" placeholder="如：联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字段标识" prop="prop">
              <el-input v-model="fieldDraft.prop" placeholder="如：contactName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字段类型" prop="type">
              <el-select
                v-model="fieldDraft.type"
                style="width: 100%"
                @change="handleFieldTypeChange"
              >
                <el-option
                  v-for="item in fieldTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="布局宽度">
              <el-select v-model="fieldDraft.span" style="width: 100%">
                <el-option :value="24" label="24 / 整行" />
                <el-option :value="12" label="12 / 半行" />
                <el-option :value="8" label="8 / 三列" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="占位提示">
              <el-input v-model="fieldDraft.placeholder" placeholder="请输入占位提示" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="帮助文本">
              <el-input v-model="fieldDraft.helpText" placeholder="如：请填写真实有效信息" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否必填">
              <el-switch v-model="fieldDraft.required" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认值">
              <el-input-number
                v-if="fieldDraft.type === 'number'"
                v-model="numberDefaultValue"
                style="width: 100%"
              />
              <el-switch
                v-else-if="fieldDraft.type === 'switch'"
                v-model="switchDefaultValue"
              />
              <el-input
                v-else
                v-model="fieldDraft.defaultValue"
                :type="fieldDraft.type === 'richtext' ? 'textarea' : 'text'"
                :rows="fieldDraft.type === 'richtext' ? 4 : undefined"
                :placeholder="fieldDraft.type === 'richtext' ? '请输入默认 HTML 内容' : '请输入默认值'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-divider content-position="left">校验规则</el-divider>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最小长度 / 最小值">
              <el-input-number v-model="fieldDraft.validation.min" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大长度 / 最大值">
              <el-input-number v-model="fieldDraft.validation.max" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="触发方式">
              <el-select v-model="fieldDraft.validation.trigger" style="width: 100%">
                <el-option value="blur" label="blur" />
                <el-option value="change" label="change" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="正则表达式">
              <el-input
                v-model="fieldDraft.validation.pattern"
                placeholder="如：^1[3-9]\\d{9}$"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="校验提示语">
              <el-input
                v-model="fieldDraft.validation.message"
                placeholder="不填写则使用默认文案"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="isSelectField" :span="24">
            <el-divider content-position="left">下拉选项</el-divider>

            <div class="record-list">
              <div
                v-for="(option, index) in fieldDraft.options"
                :key="`${option.value}-${index}`"
                class="record-card"
              >
                <el-row :gutter="12">
                  <el-col :span="10">
                    <el-input v-model="option.label" placeholder="选项名称" />
                  </el-col>
                  <el-col :span="10">
                    <el-input v-model="option.value" placeholder="选项值" />
                  </el-col>
                  <el-col :span="4">
                    <el-button type="danger" plain @click="removeOption(index)">
                      删除
                    </el-button>
                  </el-col>
                </el-row>
              </div>
            </div>

            <el-button style="margin-top: 12px" @click="addOption">新增选项</el-button>
          </el-col>

          <el-col v-if="isRemoteSelectField" :span="24">
            <el-divider content-position="left">远程下拉配置</el-divider>

            <el-row :gutter="16">
              <el-col :span="16">
                <el-form-item label="接口地址">
                  <el-input
                    v-model="remoteConfigDraft.url"
                    placeholder="如：/reviewers.json"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="请求方式">
                  <el-select v-model="remoteConfigDraft.method" style="width: 100%">
                    <el-option label="GET" value="GET" />
                    <el-option label="POST" value="POST" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="结果数组路径">
                  <el-input
                    v-model="remoteConfigDraft.resultPath"
                    placeholder="如：data.items"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="显示字段">
                  <el-input
                    v-model="remoteConfigDraft.labelKey"
                    placeholder="如：name"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="值字段">
                  <el-input
                    v-model="remoteConfigDraft.valueKey"
                    placeholder="如：id"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="关键字参数名">
                  <el-input
                    v-model="remoteConfigDraft.keywordKey"
                    placeholder="如：keyword"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <div class="field-help">
                  远程下拉会在打开面板和输入搜索词时请求接口，并按照“结果数组路径 / 显示字段 / 值字段”映射选项。
                </div>
              </el-col>
            </el-row>
          </el-col>

          <el-col v-if="fieldDraft.type === 'richtext'" :span="24">
            <el-divider content-position="left">富文本说明</el-divider>
            <div class="field-help">
              富文本字段保存为 HTML 字符串，支持基础格式化、列表和链接；长度校验会按纯文本长度计算。
            </div>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="fieldDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveField">保存字段</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
