<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Checked, Document, Promotion, Search } from '@element-plus/icons-vue'
import { apiGet, apiPost } from './api'
import DynamicFormRenderer from './components/DynamicFormRenderer.vue'
import FieldDesignerTable from './components/FieldDesignerTable.vue'

const activeMenu = ref('templates')
const users = ref([])
const versionInfo = ref({})
const templates = ref([])
const instances = ref([])
const tasks = ref([])
const selectedTask = ref(null)
const detailVisible = ref(false)
const instanceDetail = ref(null)

const loading = reactive({
  templateSaving: false,
  starting: false,
  tasks: false,
  search: false,
})

const templateForm = reactive(createEmptyTemplate())
const startForm = reactive({
  templateId: null,
  title: '',
  initiator: '',
  formData: {},
})
const taskPanel = reactive({
  assignee: '',
  formData: {},
  comment: '',
})
const searchQuery = reactive({
  keyword: '',
  initiator: '',
  status: '',
  fieldKey: '',
  fieldValue: '',
})

const selectedStartTemplate = computed(() =>
  templates.value.find((item) => item.id === startForm.templateId) || null,
)

function createField() {
  return {
    key: '',
    label: '',
    type: 'input',
    required: false,
    placeholder: '',
    optionsText: '',
  }
}

function createNode() {
  return {
    nodeKey: '',
    nodeName: '',
    assignee: '',
    formFields: [createField()],
  }
}

function createEmptyTemplate() {
  return {
    id: null,
    templateCode: '',
    name: '',
    description: '',
    startFormFields: [createField()],
    nodes: [createNode()],
  }
}

function applyTemplateForm(nextValue) {
  Object.assign(templateForm, createEmptyTemplate(), nextValue)
}

function normalizeTemplateFromServer(template) {
  return {
    id: template.id,
    templateCode: template.templateCode || '',
    name: template.name || '',
    description: template.description || '',
    startFormFields: (template.definition?.startFormFields || []).map((field) => ({
      ...field,
      optionsText: (field.options || []).join(','),
    })),
    nodes: (template.definition?.nodes || []).map((node) => ({
      nodeKey: node.nodeKey || '',
      nodeName: node.nodeName || '',
      assignee: node.assignee || '',
      formFields: (node.formFields || []).map((field) => ({
        ...field,
        optionsText: (field.options || []).join(','),
      })),
    })),
  }
}

function serializeFields(fields) {
  return (fields || []).map((field) => ({
    key: field.key,
    label: field.label,
    type: field.type,
    required: !!field.required,
    placeholder: field.placeholder,
    options: (field.optionsText || '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean),
  }))
}

function serializeTemplateForm() {
  return {
    id: templateForm.id,
    templateCode: templateForm.templateCode,
    name: templateForm.name,
    description: templateForm.description,
    startFormFields: serializeFields(templateForm.startFormFields),
    nodes: templateForm.nodes.map((node) => ({
      nodeKey: node.nodeKey,
      nodeName: node.nodeName,
      assignee: node.assignee,
      formFields: serializeFields(node.formFields),
    })),
  }
}

function addNode() {
  templateForm.nodes.push(createNode())
}

function removeNode(index) {
  templateForm.nodes.splice(index, 1)
}

function moveNode(index, direction) {
  const targetIndex = index + direction
  if (targetIndex < 0 || targetIndex >= templateForm.nodes.length) {
    return
  }
  const next = [...templateForm.nodes]
  const [node] = next.splice(index, 1)
  next.splice(targetIndex, 0, node)
  templateForm.nodes = next
}

async function fetchMeta() {
  const [userList, version] = await Promise.all([apiGet('/meta/users'), apiGet('/meta/version')])
  users.value = userList || []
  versionInfo.value = version || {}
  if (!startForm.initiator && users.value.length) {
    startForm.initiator = users.value[0]
  }
  if (!taskPanel.assignee && users.value.length) {
    taskPanel.assignee = users.value[0]
  }
}

async function fetchTemplates() {
  templates.value = await apiGet('/templates')
  if (!startForm.templateId && templates.value.length) {
    startForm.templateId = templates.value[0].id
  }
}

async function searchInstances() {
  loading.search = true
  try {
    instances.value = await apiGet('/instances', searchQuery)
  } finally {
    loading.search = false
  }
}

async function saveTemplate() {
  loading.templateSaving = true
  try {
    const saved = await apiPost('/templates', serializeTemplateForm())
    ElMessage.success(`模板已保存并部署，版本 ${saved.versionNo}`)
    await fetchTemplates()
    applyTemplateForm(normalizeTemplateFromServer(saved))
    startForm.templateId = saved.id
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.templateSaving = false
  }
}

function resetTemplate() {
  applyTemplateForm(createEmptyTemplate())
}

function editTemplate(template) {
  applyTemplateForm(normalizeTemplateFromServer(template))
  activeMenu.value = 'templates'
}

async function startWorkflow() {
  loading.starting = true
  try {
    const detail = await apiPost('/instances/start', {
      templateId: startForm.templateId,
      title: startForm.title,
      initiator: startForm.initiator,
      formData: startForm.formData,
    })
    ElMessage.success('流程发起成功')
    startForm.title = ''
    startForm.formData = {}
    await searchInstances()
    await loadTasks()
    openDetailWithData(detail)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.starting = false
  }
}

async function loadTasks() {
  loading.tasks = true
  try {
    tasks.value = await apiGet('/tasks', { assignee: taskPanel.assignee })
    selectedTask.value = tasks.value[0] || null
    taskPanel.formData = {}
    taskPanel.comment = ''
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.tasks = false
  }
}

function selectTask(task) {
  selectedTask.value = task
  taskPanel.formData = {}
  taskPanel.comment = ''
}

async function completeTask() {
  if (!selectedTask.value) {
    return
  }
  try {
    const detail = await apiPost(`/tasks/${selectedTask.value.taskId}/complete`, {
      operator: taskPanel.assignee,
      comment: taskPanel.comment,
      formData: taskPanel.formData,
    })
    ElMessage.success('任务处理完成')
    await loadTasks()
    await searchInstances()
    openDetailWithData(detail)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function openDetail(instanceId) {
  try {
    instanceDetail.value = await apiGet(`/instances/${instanceId}`)
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function openDetailWithData(detail) {
  instanceDetail.value = detail
  detailVisible.value = true
}

function formatTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ')
}

function formatValue(value) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
}

function handleMenuSelect(value) {
  activeMenu.value = value
}

onMounted(async () => {
  try {
    await fetchMeta()
    await fetchTemplates()
    await searchInstances()
    await loadTasks()
  } catch (error) {
    ElMessage.error(error.message)
  }
})
</script>

<template>
  <el-container class="app-shell">
    <el-aside width="220px" class="app-aside">
      <div class="brand">工单工作流管理系统</div>
      <el-menu :default-active="activeMenu" @select="handleMenuSelect">
        <el-menu-item index="templates">
          <el-icon><Document /></el-icon>
          <span>流程模板编排</span>
        </el-menu-item>
        <el-menu-item index="start">
          <el-icon><Promotion /></el-icon>
          <span>发起流程</span>
        </el-menu-item>
        <el-menu-item index="tasks">
          <el-icon><Checked /></el-icon>
          <span>任务办理中心</span>
        </el-menu-item>
        <el-menu-item index="instances">
          <el-icon><Search /></el-icon>
          <span>工单检索与追踪</span>
        </el-menu-item>
      </el-menu>
      <div class="version-box">
        <div>Spring Boot {{ versionInfo.springBoot }}</div>
        <div>MyBatis {{ versionInfo.mybatis }}</div>
        <div>Flowable {{ versionInfo.flowable }}</div>
      </div>
    </el-aside>

    <el-main class="app-main">
      <template v-if="activeMenu === 'templates'">
        <el-row :gutter="16">
          <el-col :span="9">
            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="card-header">
                  <span>已部署模板</span>
                  <el-button link type="primary" @click="fetchTemplates">刷新</el-button>
                </div>
              </template>
              <el-table :data="templates" stripe>
                <el-table-column prop="templateCode" label="编码" min-width="120" />
                <el-table-column prop="name" label="名称" min-width="140" />
                <el-table-column prop="versionNo" label="版本" width="70" />
                <el-table-column label="操作" width="70">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="editTemplate(row)">编辑</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="15">
            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="card-header">
                  <span>模板设计器</span>
                  <div>
                    <el-button @click="resetTemplate">新建模板</el-button>
                    <el-button type="primary" :loading="loading.templateSaving" @click="saveTemplate">
                      保存并部署
                    </el-button>
                  </div>
                </div>
              </template>
              <el-form label-position="top">
                <el-row :gutter="16">
                  <el-col :span="8">
                    <el-form-item label="模板编码">
                      <el-input v-model="templateForm.templateCode" placeholder="如 ticket_apply" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="模板名称">
                      <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="模板说明">
                      <el-input v-model="templateForm.description" placeholder="请输入模板说明" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>

              <el-divider>发起表单</el-divider>
              <FieldDesignerTable v-model="templateForm.startFormFields" />

              <el-divider>处理节点</el-divider>
              <div class="section-toolbar">
                <span>节点按顺序执行，可上下移动形成流程编排</span>
                <el-button type="primary" link @click="addNode">新增节点</el-button>
              </div>
              <el-empty v-if="!templateForm.nodes.length" description="暂无节点，请新增处理节点" />
              <el-card
                v-for="(node, index) in templateForm.nodes"
                :key="index"
                shadow="never"
                class="node-card"
              >
                <template #header>
                  <div class="card-header">
                    <span>节点 {{ index + 1 }}</span>
                    <div>
                      <el-button link @click="moveNode(index, -1)">上移</el-button>
                      <el-button link @click="moveNode(index, 1)">下移</el-button>
                      <el-button link type="danger" @click="removeNode(index)">删除</el-button>
                    </div>
                  </div>
                </template>
                <el-row :gutter="16">
                  <el-col :span="8">
                    <el-form-item label="节点编码">
                      <el-input v-model="node.nodeKey" placeholder="如 dept_review" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="节点名称">
                      <el-input v-model="node.nodeName" placeholder="如 部门审批" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="处理人员">
                      <el-select v-model="node.assignee" style="width: 100%" filterable>
                        <el-option v-for="user in users" :key="user" :label="user" :value="user" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <FieldDesignerTable v-model="node.formFields" />
              </el-card>
            </el-card>
          </el-col>
        </el-row>
      </template>

      <template v-else-if="activeMenu === 'start'">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span>发起流程</span>
              <el-button type="primary" :loading="loading.starting" @click="startWorkflow">提交工单</el-button>
            </div>
          </template>
          <el-form label-position="top">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="流程模板">
                  <el-select v-model="startForm.templateId" style="width: 100%">
                    <el-option
                      v-for="template in templates"
                      :key="template.id"
                      :label="`${template.name} (${template.templateCode})`"
                      :value="template.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="工单标题">
                  <el-input v-model="startForm.title" placeholder="请输入工单标题" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="发起人">
                  <el-select v-model="startForm.initiator" style="width: 100%" filterable>
                    <el-option v-for="user in users" :key="user" :label="user" :value="user" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <el-alert
            v-if="selectedStartTemplate"
            type="info"
            :closable="false"
            show-icon
            :title="`当前流程：${selectedStartTemplate.name}`"
            :description="selectedStartTemplate.definition.nodes.map((node) => `${node.nodeName}(${node.assignee})`).join(' → ')"
          />
          <el-divider>发起表单</el-divider>
          <DynamicFormRenderer
            :schema="selectedStartTemplate?.definition?.startFormFields || []"
            :model-value="startForm.formData"
            @update:model-value="(value) => (startForm.formData = value)"
          />
        </el-card>
      </template>

      <template v-else-if="activeMenu === 'tasks'">
        <el-row :gutter="16">
          <el-col :span="9">
            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="card-header">
                  <span>待办任务</span>
                  <div class="inline-actions">
                    <el-select v-model="taskPanel.assignee" style="width: 160px" filterable>
                      <el-option v-for="user in users" :key="user" :label="user" :value="user" />
                    </el-select>
                    <el-button type="primary" :loading="loading.tasks" @click="loadTasks">加载待办</el-button>
                  </div>
                </div>
              </template>
              <el-table :data="tasks" stripe @row-click="selectTask">
                <el-table-column prop="taskName" label="节点" min-width="120" />
                <el-table-column prop="instanceTitle" label="工单标题" min-width="150" />
                <el-table-column prop="businessKey" label="编号" min-width="160" />
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="15">
            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="card-header">
                  <span>{{ selectedTask ? `办理任务：${selectedTask.taskName}` : '请选择左侧任务' }}</span>
                  <el-button
                    v-if="selectedTask"
                    type="primary"
                    @click="completeTask"
                  >
                    提交处理
                  </el-button>
                </div>
              </template>
              <el-empty v-if="!selectedTask" description="暂无待办任务" />
              <template v-else>
                <el-descriptions :column="2" border class="detail-block">
                  <el-descriptions-item label="工单编号">{{ selectedTask.businessKey }}</el-descriptions-item>
                  <el-descriptions-item label="工单标题">{{ selectedTask.instanceTitle }}</el-descriptions-item>
                  <el-descriptions-item label="发起人">{{ selectedTask.initiator }}</el-descriptions-item>
                  <el-descriptions-item label="处理人">{{ selectedTask.assignee }}</el-descriptions-item>
                </el-descriptions>

                <el-divider>当前汇总字段</el-divider>
                <el-descriptions :column="2" border>
                  <el-descriptions-item
                    v-for="(value, key) in selectedTask.mergedFormData"
                    :key="key"
                    :label="key"
                  >
                    {{ formatValue(value) }}
                  </el-descriptions-item>
                </el-descriptions>

                <el-divider>节点处理表单</el-divider>
                <DynamicFormRenderer
                  :schema="selectedTask.formFields || []"
                  :model-value="taskPanel.formData"
                  @update:model-value="(value) => (taskPanel.formData = value)"
                />
                <el-form label-position="top">
                  <el-form-item label="处理意见">
                    <el-input v-model="taskPanel.comment" type="textarea" :rows="3" placeholder="请输入处理意见" />
                  </el-form-item>
                </el-form>
              </template>
            </el-card>
          </el-col>
        </el-row>
      </template>

      <template v-else>
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span>工单检索</span>
              <el-button type="primary" :loading="loading.search" @click="searchInstances">查询</el-button>
            </div>
          </template>
          <el-form label-position="top">
            <el-row :gutter="16">
              <el-col :span="6">
                <el-form-item label="关键词">
                  <el-input v-model="searchQuery.keyword" placeholder="标题/汇总字段/编号" />
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item label="发起人">
                  <el-select v-model="searchQuery.initiator" clearable style="width: 100%">
                    <el-option v-for="user in users" :key="user" :label="user" :value="user" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item label="状态">
                  <el-select v-model="searchQuery.status" clearable style="width: 100%">
                    <el-option label="运行中" value="RUNNING" />
                    <el-option label="已完成" value="COMPLETED" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="字段Key">
                  <el-input v-model="searchQuery.fieldKey" placeholder="如 amount" />
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="字段值">
                  <el-input v-model="searchQuery.fieldValue" placeholder="模糊匹配字段值" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <el-table :data="instances" stripe @row-click="(row) => openDetail(row.id)">
            <el-table-column prop="businessKey" label="编号" min-width="170" />
            <el-table-column prop="templateName" label="模板" min-width="150" />
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column prop="initiator" label="发起人" width="110" />
            <el-table-column prop="currentNodeName" label="当前节点" min-width="120" />
            <el-table-column prop="status" label="状态" width="110" />
            <el-table-column label="更新时间" min-width="160">
              <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </template>
    </el-main>
  </el-container>

  <el-drawer v-model="detailVisible" title="工单详情" size="60%">
    <template v-if="instanceDetail">
      <el-descriptions :column="2" border class="detail-block">
        <el-descriptions-item label="工单编号">{{ instanceDetail.businessKey }}</el-descriptions-item>
        <el-descriptions-item label="模板">{{ instanceDetail.templateName }}</el-descriptions-item>
        <el-descriptions-item label="工单标题">{{ instanceDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ instanceDetail.initiator }}</el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ instanceDetail.currentNodeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ instanceDetail.status }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>最终汇总字段</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item
          v-for="field in instanceDetail.indexedFields"
          :key="field.fieldKey"
          :label="field.fieldLabel || field.fieldKey"
        >
          {{ formatValue(field.fieldValue) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider>处理留痕</el-divider>
      <el-table :data="instanceDetail.records" stripe>
        <el-table-column prop="action" label="动作" width="100" />
        <el-table-column prop="taskName" label="节点" min-width="120" />
        <el-table-column prop="operator" label="处理人" width="120" />
        <el-table-column prop="comment" label="意见" min-width="180" />
        <el-table-column label="提交字段" min-width="220">
          <template #default="{ row }">{{ formatValue(row.submittedFormData) }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </template>
  </el-drawer>
</template>
