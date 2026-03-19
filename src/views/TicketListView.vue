<script setup lang="ts">
import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { useTicketWorkbenchStore } from '../stores/ticketWorkbenchStore'
import { useAuthStore } from '../stores/authStore'
import { formatDateTime, sourceLabelMap, statusLabelMap, statusTagTypeMap } from '../utils/ticketSystem'

const router = useRouter()
const store = useTicketWorkbenchStore()
const authStore = useAuthStore()

const filters = reactive({
  keyword: '',
  status: '',
  categoryName: '',
})

const createDialogVisible = reactive({
  value: false,
})

const createForm = reactive({
  title: '',
  description: '',
  contactPhone: '13800001234',
  customerName: '华东大客户中心',
  departmentName: authStore.currentUser?.departmentName || '客户成功部',
  ticketType: '故障',
  categoryName: '系统故障',
  priority: 'P2',
})

const categories = computed(() =>
  [...new Set(store.tickets.map((ticket) => ticket.categoryName))],
)

const filteredTickets = computed(() =>
  store.tickets.filter((ticket) => {
    const matchKeyword =
      !filters.keyword ||
      ticket.ticketNo.includes(filters.keyword) ||
      ticket.title.includes(filters.keyword)

    const matchStatus = !filters.status || ticket.status === filters.status
    const matchCategory =
      !filters.categoryName || ticket.categoryName === filters.categoryName

    return matchKeyword && matchStatus && matchCategory
  }),
)

const openCreateDialog = () => {
  createDialogVisible.value = true
}

const submitCreate = () => {
  if (!createForm.title || !createForm.description) {
    ElMessage.warning('请填写工单标题和问题描述')
    return
  }

  const category = {
    账号权限: 'ACCOUNT',
    系统故障: 'SYSTEM_ERROR',
    接口异常: 'API_ERROR',
    性能问题: 'PERFORMANCE',
    报表问题: 'REPORT',
    环境部署: 'DEPLOY',
  }[createForm.categoryName]

  store.createTicket({
    title: createForm.title,
    description: createForm.description,
    requesterName: authStore.currentUser?.displayName || '提单人',
    contactPhone: createForm.contactPhone,
    customerName: createForm.customerName,
    departmentName: createForm.departmentName,
    ticketType: createForm.ticketType as '咨询' | '故障' | '需求' | '变更',
    categoryCode: category || 'SYSTEM_ERROR',
    categoryName: createForm.categoryName,
    priority: createForm.priority as 'P1' | 'P2' | 'P3' | 'P4',
  })

  createDialogVisible.value = false
  createForm.title = ''
  createForm.description = ''
  ElMessage.success('工单已创建并进入待受理队列')
}
</script>

<template>
  <div class="page-stack">
    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div>
            <div class="panel-title">工单列表</div>
            <div class="panel-desc">支持按编号、标题、状态和分类进行筛选</div>
          </div>
          <el-button
            v-if="authStore.hasButtonPermission('ticket:create')"
            type="primary"
            @click="openCreateDialog"
          >
            新建工单
          </el-button>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :span="8">
          <el-input v-model="filters.keyword" placeholder="搜索工单编号或标题" clearable />
        </el-col>
        <el-col :span="8">
          <el-select v-model="filters.status" placeholder="筛选状态" clearable style="width: 100%">
            <el-option
              v-for="(label, value) in statusLabelMap"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select v-model="filters.categoryName" placeholder="筛选分类" clearable style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-col>
      </el-row>

      <el-table :data="filteredTickets" border style="margin-top: 18px">
        <el-table-column prop="ticketNo" label="工单编号" min-width="160" />
        <el-table-column prop="title" label="标题" min-width="260" />
        <el-table-column label="来源" width="120">
          <template #default="{ row }">{{ sourceLabelMap[row.source] }}</template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="priority" label="优先级" width="90" />
        <el-table-column prop="currentAssigneeName" label="处理人" width="120" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="statusTagTypeMap[row.status]">{{ statusLabelMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否超时" width="100">
          <template #default="{ row }">
            <el-tag :type="row.timeout ? 'danger' : 'success'">
              {{ row.timeout ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="router.push(`/tickets/${row.ticketId}`)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createDialogVisible.value" title="新建工单" width="720px">
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工单标题">
              <el-input v-model="createForm.title" placeholder="请输入工单标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单类型">
              <el-select v-model="createForm.ticketType" style="width: 100%">
                <el-option label="咨询" value="咨询" />
                <el-option label="故障" value="故障" />
                <el-option label="需求" value="需求" />
                <el-option label="变更" value="变更" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单分类">
              <el-select v-model="createForm.categoryName" style="width: 100%">
                <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="createForm.priority" style="width: 100%">
                <el-option label="P1" value="P1" />
                <el-option label="P2" value="P2" />
                <el-option label="P3" value="P3" />
                <el-option label="P4" value="P4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户/部门">
              <el-input v-model="createForm.customerName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式">
              <el-input v-model="createForm.contactPhone" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="问题描述">
              <el-input v-model="createForm.description" type="textarea" :rows="5" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="chip-row" style="justify-content: flex-end">
          <el-button @click="createDialogVisible.value = false">取消</el-button>
          <el-button type="primary" @click="submitCreate">提交工单</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
