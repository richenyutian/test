<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Promotion, Search, Setting, Share } from '@element-plus/icons-vue'
import { apiGet, apiPost } from './api'
import DynamicFormRenderer from './components/DynamicFormRenderer.vue'
import FieldDesignerTable from './components/FieldDesignerTable.vue'

const activeMenu = ref('forms')
const versionInfo = ref({})
const users = ref([])
const forms = ref([])
const nodes = ref([])
const flows = ref([])
const tickets = ref([])
const tasks = ref([])
const selectedTask = ref(null)
const ticketDetail = ref(null)
const detailVisible = ref(false)

const loading = reactive({
  forms: false,
  nodes: false,
  flows: false,
  tickets: false,
  tasks: false,
  savingForm: false,
  savingNode: false,
  savingFlow: false,
  starting: false,
  completing: false,
})

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

function createTransition() {
  return {
    fromNodeId: null,
    toNodeId: null,
    transitionName: '',
    sortNo: 0,
  }
}

function emptyFormEditor() {
  return {
    id: null,
    formCode: '',
    name: '',
    description: '',
    fields: [createField()],
  }
}

function emptyNodeEditor() {
  return {
    id: null,
    nodeCode: '',
    name: '',
    nodeState: 'START',
    formId: null,
    assignee: '',
    actionType: 'SUBMIT',
    description: '',
  }
}

function emptyFlowEditor() {
  return {
    id: null,
    flowCode: '',
    name: '',
    description: '',
    startNodeId: null,
    nodeIds: [],
    transitions: [createTransition()],
  }
}

const formEditor = reactive(emptyFormEditor())
const nodeEditor = reactive(emptyNodeEditor())
const flowEditor = reactive(emptyFlowEditor())
const startTicketEditor = reactive({
  flowId: null,
  title: '',
  applicant: '',
  formData: {},
  nextNodeId: null,
})
const taskAction = reactive({
  assignee: '',
  formData: {},
  comment: '',
  nextNodeId: null,
})
const ticketSearch = reactive({
  keyword: '',
  status: '',
})

const selectedFlow = computed(() => flows.value.find((item) => item.id === startTicketEditor.flowId) || null)
const startNode = computed(() =>
  selectedFlow.value?.nodes?.find((item) => item.id === selectedFlow.value.startNodeId) || null,
)
const startNextOptions = computed(() =>
  (selectedFlow.value?.transitions || [])
    .filter((item) => item.fromNodeId === selectedFlow.value?.startNodeId)
    .map((item) => ({
      nodeId: item.toNodeId,
      nodeName: item.toNodeName,
      nodeState: nodes.value.find((node) => node.id === item.toNodeId)?.nodeState || '',
    })),
)
const flowSelectedNodes = computed(() =>
  nodes.value.filter((item) => flowEditor.nodeIds.includes(item.id)),
)

function handleMenuSelect(value) {
  activeMenu.value = value
}

function normalizeFieldEdit(field) {
  return {
    key: field.key || '',
    label: field.label || '',
    type: field.type || 'input',
    required: !!field.required,
    placeholder: field.placeholder || '',
    optionsText: (field.options || []).join(','),
  }
}

function applyFormEditor(payload) {
  Object.assign(formEditor, emptyFormEditor(), payload)
}

function applyNodeEditor(payload) {
  Object.assign(nodeEditor, emptyNodeEditor(), payload)
}

function applyFlowEditor(payload) {
  Object.assign(flowEditor, emptyFlowEditor(), payload)
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

async function fetchMeta() {
  const [version, demoUsers] = await Promise.all([
    apiGet('/meta/version'),
    apiGet('/meta/users'),
  ])
  versionInfo.value = version || {}
  users.value = demoUsers || []
  if (!nodeEditor.assignee && users.value.length) {
    nodeEditor.assignee = users.value[0]
  }
  if (!startTicketEditor.applicant && users.value.length) {
    startTicketEditor.applicant = users.value[1] || users.value[0]
  }
  if (!taskAction.assignee && users.value.length) {
    taskAction.assignee = users.value[1] || users.value[0]
  }
}

async function fetchForms() {
  loading.forms = true
  try {
    forms.value = await apiGet('/forms')
  } finally {
    loading.forms = false
  }
}

async function fetchNodes() {
  loading.nodes = true
  try {
    nodes.value = await apiGet('/nodes')
  } finally {
    loading.nodes = false
  }
}

async function fetchFlows() {
  loading.flows = true
  try {
    flows.value = await apiGet('/flows')
    if (!startTicketEditor.flowId && flows.value.length) {
      startTicketEditor.flowId = flows.value[0].id
    }
  } finally {
    loading.flows = false
  }
}

async function fetchTickets() {
  loading.tickets = true
  try {
    tickets.value = await apiGet('/tickets', ticketSearch)
  } finally {
    loading.tickets = false
  }
}

async function fetchTasks() {
  loading.tasks = true
  try {
    tasks.value = await apiGet('/tasks', { assignee: taskAction.assignee })
    selectedTask.value = tasks.value[0] || null
    taskAction.formData = {}
    taskAction.comment = ''
    taskAction.nextNodeId = selectedTask.value?.nextNodeOptions?.length === 1 ? selectedTask.value.nextNodeOptions[0].nodeId : null
  } finally {
    loading.tasks = false
  }
}

async function saveForm() {
  loading.savingForm = true
  try {
    await apiPost('/forms', {
      id: formEditor.id,
      formCode: formEditor.formCode,
      name: formEditor.name,
      description: formEditor.description,
      fields: serializeFields(formEditor.fields),
    })
    ElMessage.success('表单保存成功')
    applyFormEditor(emptyFormEditor())
    await fetchForms()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.savingForm = false
  }
}

async function saveNode() {
  loading.savingNode = true
  try {
    await apiPost('/nodes', { ...nodeEditor })
    ElMessage.success('节点保存成功')
    applyNodeEditor(emptyNodeEditor())
    if (users.value.length) {
      nodeEditor.assignee = users.value[0]
    }
    await fetchNodes()
    await fetchFlows()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.savingNode = false
  }
}

async function saveFlow() {
  loading.savingFlow = true
  try {
    await apiPost('/flows', {
      id: flowEditor.id,
      flowCode: flowEditor.flowCode,
      name: flowEditor.name,
      description: flowEditor.description,
      startNodeId: flowEditor.startNodeId,
      nodeIds: flowEditor.nodeIds,
      transitions: flowEditor.transitions,
    })
    ElMessage.success('流程保存成功')
    applyFlowEditor(emptyFlowEditor())
    await fetchFlows()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.savingFlow = false
  }
}

async function startTicket() {
  loading.starting = true
  try {
    const detail = await apiPost('/tickets/start', {
      flowId: startTicketEditor.flowId,
      title: startTicketEditor.title,
      applicant: startTicketEditor.applicant,
      formData: startTicketEditor.formData,
      nextNodeId: startTicketEditor.nextNodeId,
    })
    ElMessage.success('工单发起成功')
    startTicketEditor.title = ''
    startTicketEditor.formData = {}
    startTicketEditor.nextNodeId = startNextOptions.value.length === 1 ? startNextOptions.value[0].nodeId : null
    await fetchTickets()
    await fetchTasks()
    openTicketDetailWithData(detail)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.starting = false
  }
}

async function completeTask() {
  if (!selectedTask.value) {
    return
  }
  loading.completing = true
  try {
    const detail = await apiPost(`/tasks/${selectedTask.value.taskId}/complete`, {
      operator: taskAction.assignee,
      comment: taskAction.comment,
      formData: taskAction.formData,
      nextNodeId: taskAction.nextNodeId,
    })
    ElMessage.success('节点处理完成')
    taskAction.formData = {}
    taskAction.comment = ''
    taskAction.nextNodeId = null
    await fetchTasks()
    await fetchTickets()
    openTicketDetailWithData(detail)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.completing = false
  }
}

function editForm(row) {
  applyFormEditor({
    id: row.id,
    formCode: row.formCode,
    name: row.name,
    description: row.description,
    fields: (row.fields || []).map(normalizeFieldEdit),
  })
  activeMenu.value = 'forms'
}

function editNode(row) {
  applyNodeEditor({
    id: row.id,
    nodeCode: row.nodeCode,
    name: row.name,
    nodeState: row.nodeState,
    formId: row.formId,
    assignee: row.assignee,
    actionType: row.actionType,
    description: row.description,
  })
  activeMenu.value = 'nodes'
}

function editFlow(row) {
  applyFlowEditor({
    id: row.id,
    flowCode: row.flowCode,
    name: row.name,
    description: row.description,
    startNodeId: row.startNodeId,
    nodeIds: (row.nodes || []).map((item) => item.id),
    transitions: (row.transitions || []).map((item) => ({
      fromNodeId: item.fromNodeId,
      toNodeId: item.toNodeId,
      transitionName: item.transitionName,
      sortNo: item.sortNo,
    })),
  })
  activeMenu.value = 'flows'
}

function addTransition() {
  flowEditor.transitions.push(createTransition())
}

function removeTransition(index) {
  flowEditor.transitions.splice(index, 1)
}

function onFlowChanged() {
  startTicketEditor.formData = {}
  startTicketEditor.nextNodeId = startNextOptions.value.length === 1 ? startNextOptions.value[0].nodeId : null
}

function selectTask(row) {
  selectedTask.value = row
  taskAction.formData = {}
  taskAction.comment = ''
  taskAction.nextNodeId = row.nextNodeOptions?.length === 1 ? row.nextNodeOptions[0].nodeId : null
}

async function openTicketDetail(ticketId) {
  try {
    ticketDetail.value = await apiGet(`/tickets/${ticketId}`)
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function openTicketDetailWithData(data) {
  ticketDetail.value = data
  detailVisible.value = true
}

function resetFormEditor() {
  applyFormEditor(emptyFormEditor())
}

function resetNodeEditor() {
  applyNodeEditor(emptyNodeEditor())
  if (users.value.length) {
    nodeEditor.assignee = users.value[0]
  }
}

function resetFlowEditor() {
  applyFlowEditor(emptyFlowEditor())
}

function formatValue(value) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
}

function formatTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ')
}

onMounted(async () => {
  try {
    await fetchMeta()
    await Promise.all([fetchForms(), fetchNodes(), fetchFlows(), fetchTickets()])
    await fetchTasks()
  } catch (error) {
    ElMessage.error(error.message)
  }
})
</script>

<template>
  <el-container class="layout-shell">
    <el-aside width="240px" class="layout-aside">
      <div class="brand-box">
        <div class="brand-title">Ticket System</div>
        <div class="brand-subtitle">现代化工单后台</div>
      </div>
      <el-menu :default-active="activeMenu" class="menu-panel" @select="handleMenuSelect">
        <el-menu-item index="forms"><el-icon><Document /></el-icon><span>表单管理</span></el-menu-item>
        <el-menu-item index="nodes"><el-icon><Setting /></el-icon><span>节点管理</span></el-menu-item>
        <el-menu-item index="flows"><el-icon><Share /></el-icon><span>流程管理</span></el-menu-item>
        <el-menu-item index="start"><el-icon><Promotion /></el-icon><span>发起工单</span></el-menu-item>
        <el-menu-item index="tasks"><el-icon><Document /></el-icon><span>待办中心</span></el-menu-item>
        <el-menu-item index="tickets"><el-icon><Search /></el-icon><span>工单列表</span></el-menu-item>
      </el-menu>
      <div class="tech-box">
        <div>JDK {{ versionInfo.java }}</div>
        <div>Maven {{ versionInfo.maven }}</div>
        <div>PostgreSQL {{ versionInfo.postgresql }}</div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div>
          <div class="header-title">工单系统管理台</div>
          <div class="header-subtitle">参考 vue-pure-admin 的简洁后台风格，覆盖表单、节点、流程与运行中心</div>
        </div>
      </el-header>

      <el-main class="layout-main">
        <template v-if="activeMenu === 'forms'">
          <el-row :gutter="16">
            <el-col :span="9">
              <el-card shadow="never" class="section-card">
                <template #header>
                  <div class="card-header">
                    <span>表单列表</span>
                    <el-button link type="primary" @click="fetchForms">刷新</el-button>
                  </div>
                </template>
                <el-table :data="forms" stripe>
                  <el-table-column prop="formCode" label="编码" min-width="120" />
                  <el-table-column prop="name" label="名称" min-width="120" />
                  <el-table-column label="字段数" width="80">
                    <template #default="{ row }">{{ row.fields?.length || 0 }}</template>
                  </el-table-column>
                  <el-table-column label="操作" width="70">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="editForm(row)">编辑</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
            <el-col :span="15">
              <el-card shadow="never" class="section-card">
                <template #header>
                  <div class="card-header">
                    <span>表单设计器</span>
                    <div>
                      <el-button @click="resetFormEditor">新建</el-button>
                      <el-button type="primary" :loading="loading.savingForm" @click="saveForm">保存表单</el-button>
                    </div>
                  </div>
                </template>
                <el-form label-position="top">
                  <el-row :gutter="16">
                    <el-col :span="8">
                      <el-form-item label="表单编码">
                        <el-input v-model="formEditor.formCode" placeholder="如 ticket_apply" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="表单名称">
                        <el-input v-model="formEditor.name" placeholder="请输入表单名称" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="表单说明">
                        <el-input v-model="formEditor.description" placeholder="请输入说明" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
                <FieldDesignerTable v-model="formEditor.fields" />
              </el-card>
            </el-col>
          </el-row>
        </template>

        <template v-else-if="activeMenu === 'nodes'">
          <el-row :gutter="16">
            <el-col :span="10">
              <el-card shadow="never" class="section-card">
                <template #header>
                  <div class="card-header">
                    <span>节点列表</span>
                    <el-button link type="primary" @click="fetchNodes">刷新</el-button>
                  </div>
                </template>
                <el-table :data="nodes" stripe>
                  <el-table-column prop="nodeCode" label="编码" min-width="120" />
                  <el-table-column prop="name" label="节点名称" min-width="120" />
                  <el-table-column prop="nodeState" label="状态" width="100" />
                  <el-table-column prop="assignee" label="处理人" width="110" />
                  <el-table-column label="操作" width="70">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="editNode(row)">编辑</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
            <el-col :span="14">
              <el-card shadow="never" class="section-card">
                <template #header>
                  <div class="card-header">
                    <span>节点编辑</span>
                    <div>
                      <el-button @click="resetNodeEditor">新建</el-button>
                      <el-button type="primary" :loading="loading.savingNode" @click="saveNode">保存节点</el-button>
                    </div>
                  </div>
                </template>
                <el-form label-position="top">
                  <el-row :gutter="16">
                    <el-col :span="8">
                      <el-form-item label="节点编码">
                        <el-input v-model="nodeEditor.nodeCode" placeholder="如 review_manager" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="节点名称">
                        <el-input v-model="nodeEditor.name" placeholder="请输入节点名称" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="节点状态">
                        <el-select v-model="nodeEditor.nodeState" style="width: 100%">
                          <el-option label="开始" value="START" />
                          <el-option label="审核" value="AUDIT" />
                          <el-option label="处理" value="HANDLE" />
                          <el-option label="关闭" value="CLOSE" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-row :gutter="16">
                    <el-col :span="8">
                      <el-form-item label="表单 ID">
                        <el-select v-model="nodeEditor.formId" style="width: 100%" filterable>
                          <el-option
                            v-for="form in forms"
                            :key="form.id"
                            :label="`${form.name} (#${form.id})`"
                            :value="form.id"
                          />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="处理人">
                        <el-select v-model="nodeEditor.assignee" style="width: 100%" filterable>
                          <el-option v-for="user in users" :key="user" :label="user" :value="user" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="执行动作">
                        <el-select v-model="nodeEditor.actionType" style="width: 100%">
                          <el-option label="提交" value="SUBMIT" />
                          <el-option label="审核" value="APPROVE" />
                          <el-option label="处理" value="PROCESS" />
                          <el-option label="关闭" value="FINISH" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item label="节点说明">
                    <el-input v-model="nodeEditor.description" type="textarea" :rows="4" placeholder="请输入节点说明" />
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
        </template>

        <template v-else-if="activeMenu === 'flows'">
          <el-row :gutter="16">
            <el-col :span="10">
              <el-card shadow="never" class="section-card">
                <template #header>
                  <div class="card-header">
                    <span>流程列表</span>
                    <el-button link type="primary" @click="fetchFlows">刷新</el-button>
                  </div>
                </template>
                <el-table :data="flows" stripe>
                  <el-table-column prop="flowCode" label="编码" min-width="120" />
                  <el-table-column prop="name" label="流程名称" min-width="140" />
                  <el-table-column label="节点数" width="80">
                    <template #default="{ row }">{{ row.nodes?.length || 0 }}</template>
                  </el-table-column>
                  <el-table-column label="操作" width="70">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="editFlow(row)">编辑</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
            <el-col :span="14">
              <el-card shadow="never" class="section-card">
                <template #header>
                  <div class="card-header">
                    <span>流程编排</span>
                    <div>
                      <el-button @click="resetFlowEditor">新建</el-button>
                      <el-button type="primary" :loading="loading.savingFlow" @click="saveFlow">保存流程</el-button>
                    </div>
                  </div>
                </template>
                <el-form label-position="top">
                  <el-row :gutter="16">
                    <el-col :span="8">
                      <el-form-item label="流程编码">
                        <el-input v-model="flowEditor.flowCode" placeholder="如 service_request" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="流程名称">
                        <el-input v-model="flowEditor.name" placeholder="请输入流程名称" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="流程说明">
                        <el-input v-model="flowEditor.description" placeholder="请输入流程说明" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-row :gutter="16">
                    <el-col :span="12">
                      <el-form-item label="流程节点">
                        <el-select v-model="flowEditor.nodeIds" multiple filterable style="width: 100%">
                          <el-option
                            v-for="node in nodes"
                            :key="node.id"
                            :label="`${node.name} (${node.nodeState})`"
                            :value="node.id"
                          />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="开始节点">
                        <el-select v-model="flowEditor.startNodeId" style="width: 100%">
                          <el-option
                            v-for="node in flowSelectedNodes.filter((item) => item.nodeState === 'START')"
                            :key="node.id"
                            :label="node.name"
                            :value="node.id"
                          />
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>

                <el-alert
                  type="info"
                  :closable="false"
                  show-icon
                  description="流程管理通过节点和流转关系表达业务动作方向；非关闭节点至少需要一个 next 节点，关闭节点不再流转。"
                />

                <el-divider>流转关系</el-divider>
                <div class="section-toolbar">
                  <span>一个节点可配置多个 next 节点</span>
                  <el-button type="primary" link @click="addTransition">新增流转</el-button>
                </div>
                <el-empty v-if="!flowEditor.transitions.length" description="暂无流转关系" />
                <el-card v-for="(transition, index) in flowEditor.transitions" :key="index" shadow="never" class="field-card">
                  <el-row :gutter="12">
                    <el-col :span="7">
                      <el-select v-model="transition.fromNodeId" style="width: 100%">
                        <el-option
                          v-for="node in flowSelectedNodes"
                          :key="node.id"
                          :label="`${node.name} (${node.nodeState})`"
                          :value="node.id"
                        />
                      </el-select>
                    </el-col>
                    <el-col :span="7">
                      <el-select v-model="transition.toNodeId" style="width: 100%">
                        <el-option
                          v-for="node in flowSelectedNodes"
                          :key="node.id"
                          :label="`${node.name} (${node.nodeState})`"
                          :value="node.id"
                        />
                      </el-select>
                    </el-col>
                    <el-col :span="6">
                      <el-input v-model="transition.transitionName" placeholder="流转名称，如 通过" />
                    </el-col>
                    <el-col :span="2">
                      <el-input-number v-model="transition.sortNo" :min="0" style="width: 100%" />
                    </el-col>
                    <el-col :span="2" class="align-right">
                      <el-button type="danger" link @click="removeTransition(index)">删除</el-button>
                    </el-col>
                  </el-row>
                </el-card>
              </el-card>
            </el-col>
          </el-row>
        </template>

        <template v-else-if="activeMenu === 'start'">
          <el-card shadow="never" class="section-card">
            <template #header>
              <div class="card-header">
                <span>发起工单</span>
                <el-button type="primary" :loading="loading.starting" @click="startTicket">提交工单</el-button>
              </div>
            </template>
            <el-form label-position="top">
              <el-row :gutter="16">
                <el-col :span="8">
                  <el-form-item label="流程">
                    <el-select v-model="startTicketEditor.flowId" style="width: 100%" @change="onFlowChanged">
                      <el-option
                        v-for="flow in flows"
                        :key="flow.id"
                        :label="`${flow.name} (${flow.flowCode})`"
                        :value="flow.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="工单标题">
                    <el-input v-model="startTicketEditor.title" placeholder="请输入工单标题" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="发起人">
                    <el-select v-model="startTicketEditor.applicant" style="width: 100%" filterable>
                      <el-option v-for="user in users" :key="user" :label="user" :value="user" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
            <el-alert
              v-if="selectedFlow"
              type="info"
              :closable="false"
              show-icon
              :title="selectedFlow.name"
              :description="`开始节点：${startNode?.name || '-'}；共 ${selectedFlow.nodes?.length || 0} 个节点`"
            />
            <el-divider>开始节点表单</el-divider>
            <DynamicFormRenderer
              :schema="startNode?.formFields || []"
              :model-value="startTicketEditor.formData"
              @update:model-value="(value) => (startTicketEditor.formData = value)"
            />
            <el-form v-if="startNextOptions.length > 1" label-position="top">
              <el-form-item label="选择下一节点">
                <el-select v-model="startTicketEditor.nextNodeId" style="width: 300px">
                  <el-option
                    v-for="option in startNextOptions"
                    :key="option.nodeId"
                    :label="`${option.nodeName} (${option.nodeState})`"
                    :value="option.nodeId"
                  />
                </el-select>
              </el-form-item>
            </el-form>
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
                      <el-select v-model="taskAction.assignee" style="width: 160px" filterable>
                        <el-option v-for="user in users" :key="user" :label="user" :value="user" />
                      </el-select>
                      <el-button type="primary" :loading="loading.tasks" @click="fetchTasks">刷新</el-button>
                    </div>
                  </div>
                </template>
                <el-table :data="tasks" stripe @row-click="selectTask">
                  <el-table-column prop="ticketNo" label="工单编号" min-width="160" />
                  <el-table-column prop="title" label="标题" min-width="160" />
                  <el-table-column prop="nodeName" label="当前节点" min-width="120" />
                </el-table>
              </el-card>
            </el-col>
            <el-col :span="15">
              <el-card shadow="never" class="section-card">
                <template #header>
                  <div class="card-header">
                    <span>{{ selectedTask ? `办理任务：${selectedTask.nodeName}` : '请选择左侧待办任务' }}</span>
                    <el-button v-if="selectedTask" type="primary" :loading="loading.completing" @click="completeTask">提交处理</el-button>
                  </div>
                </template>
                <el-empty v-if="!selectedTask" description="暂无待办任务" />
                <template v-else>
                  <el-descriptions :column="2" border class="detail-block">
                    <el-descriptions-item label="工单编号">{{ selectedTask.ticketNo }}</el-descriptions-item>
                    <el-descriptions-item label="工单标题">{{ selectedTask.title }}</el-descriptions-item>
                    <el-descriptions-item label="节点状态">{{ selectedTask.nodeState }}</el-descriptions-item>
                    <el-descriptions-item label="处理人">{{ selectedTask.assignee }}</el-descriptions-item>
                  </el-descriptions>

                  <el-divider>当前业务数据快照</el-divider>
                  <el-descriptions :column="2" border>
                    <el-descriptions-item
                      v-for="(value, key) in selectedTask.businessData"
                      :key="key"
                      :label="key"
                    >
                      {{ formatValue(value) }}
                    </el-descriptions-item>
                  </el-descriptions>

                  <el-divider>节点表单</el-divider>
                  <DynamicFormRenderer
                    :schema="selectedTask.formFields || []"
                    :model-value="taskAction.formData"
                    @update:model-value="(value) => (taskAction.formData = value)"
                  />
                  <el-form label-position="top">
                    <el-form-item v-if="selectedTask.nextNodeOptions?.length > 1" label="选择下一节点">
                      <el-select v-model="taskAction.nextNodeId" style="width: 320px">
                        <el-option
                          v-for="option in selectedTask.nextNodeOptions"
                          :key="option.nodeId"
                          :label="`${option.nodeName} (${option.nodeState})`"
                          :value="option.nodeId"
                        />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="处理意见">
                      <el-input v-model="taskAction.comment" type="textarea" :rows="4" placeholder="请输入处理意见" />
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
                <span>工单列表</span>
                <el-button type="primary" :loading="loading.tickets" @click="fetchTickets">查询</el-button>
              </div>
            </template>
            <el-form label-position="top">
              <el-row :gutter="16">
                <el-col :span="8">
                  <el-form-item label="关键词">
                    <el-input v-model="ticketSearch.keyword" placeholder="工单号 / 标题 / 发起人 / 流程名" />
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item label="状态">
                    <el-select v-model="ticketSearch.status" clearable style="width: 100%">
                      <el-option label="进行中" value="IN_PROGRESS" />
                      <el-option label="已完成" value="COMPLETED" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
            <el-table :data="tickets" stripe @row-click="(row) => openTicketDetail(row.id)">
              <el-table-column prop="ticketNo" label="工单编号" min-width="170" />
              <el-table-column prop="flowName" label="流程" min-width="140" />
              <el-table-column prop="title" label="标题" min-width="180" />
              <el-table-column prop="applicant" label="发起人" width="110" />
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
  </el-container>

  <el-drawer v-model="detailVisible" size="60%" title="工单详情">
    <template v-if="ticketDetail">
      <el-descriptions :column="2" border class="detail-block">
        <el-descriptions-item label="工单编号">{{ ticketDetail.ticketNo }}</el-descriptions-item>
        <el-descriptions-item label="流程">{{ ticketDetail.flowName }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ ticketDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ ticketDetail.applicant }}</el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ ticketDetail.currentNodeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ ticketDetail.status }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>业务数据统一快照</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item
          v-for="(value, key) in ticketDetail.businessData"
          :key="key"
          :label="key"
        >
          {{ formatValue(value) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider>处理记录</el-divider>
      <el-table :data="ticketDetail.records" stripe>
        <el-table-column prop="nodeName" label="节点" min-width="120" />
        <el-table-column prop="nodeState" label="状态" width="100" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="actionType" label="动作" width="100" />
        <el-table-column prop="comment" label="意见" min-width="180" />
        <el-table-column label="提交数据" min-width="220">
          <template #default="{ row }">{{ formatValue(row.submittedData) }}</template>
        </el-table-column>
        <el-table-column label="流转到" min-width="140">
          <template #default="{ row }">{{ row.selectedNextNodeName || '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </template>
  </el-drawer>
</template>
