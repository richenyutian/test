<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getTicketPageApi } from '../../api/modules/ticket'
import StatusTag from '../../components/common/StatusTag.vue'
import type { PageResponse } from '../../types/api'
import type { TicketListItem } from '../../types/ticket'
import { formatDateTime } from '../../utils/date'
import { categoryLabelMap, priorityLabelMap, sourceLabelMap, ticketTypeLabelMap } from '../../utils/dict'

const router = useRouter()
const loading = ref(false)
const filters = reactive({
  keyword: '',
  currentStatus: '',
  priorityCode: '',
})

const page = reactive<PageResponse<TicketListItem>>({
  current: 1,
  size: 10,
  total: 0,
  records: [],
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await getTicketPageApi({
      current: page.current,
      size: page.size,
      keyword: filters.keyword || undefined,
      currentStatus: filters.currentStatus || undefined,
      priorityCode: filters.priorityCode || undefined,
    })
    Object.assign(page, data)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <el-card class="module-card">
      <template #header>
        <div class="toolbar">
          <div class="toolbar__left">
            <el-input v-model="filters.keyword" placeholder="搜索工单编号或标题" clearable style="width: 260px" />
            <el-select v-model="filters.currentStatus" clearable placeholder="状态筛选" style="width: 180px">
              <el-option label="待受理" value="PENDING_ACCEPT" />
              <el-option label="已受理" value="ACCEPTED" />
              <el-option label="待分派" value="PENDING_ASSIGN" />
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="待用户确认" value="PENDING_CONFIRM" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已关闭" value="CLOSED" />
              <el-option label="已挂起" value="SUSPENDED" />
              <el-option label="已重开" value="REOPENED" />
            </el-select>
            <el-select v-model="filters.priorityCode" clearable placeholder="优先级" style="width: 140px">
              <el-option label="P1" value="P1" />
              <el-option label="P2" value="P2" />
              <el-option label="P3" value="P3" />
              <el-option label="P4" value="P4" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
          </div>

          <el-button v-permission="'ticket:create'" type="primary" @click="router.push('/tickets/create')">
            创建工单
          </el-button>
        </div>
      </template>

      <el-table :data="page.records" border v-loading="loading">
        <el-table-column prop="ticketNo" label="工单编号" min-width="160" />
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="requesterName" label="提单人" min-width="120" />
        <el-table-column label="来源" width="120">
          <template #default="{ row }">{{ sourceLabelMap[row.sourceCode] || row.sourceCode }}</template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ ticketTypeLabelMap[row.ticketTypeCode] || row.ticketTypeCode }}</template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">{{ categoryLabelMap[row.categoryCode] || row.categoryCode }}</template>
        </el-table-column>
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">{{ priorityLabelMap[row.priorityCode] || row.priorityCode }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :status="row.currentStatus" />
          </template>
        </el-table-column>
        <el-table-column prop="currentHandlerName" label="当前处理人" min-width="120" />
        <el-table-column label="超时" width="100">
          <template #default="{ row }">
            <el-tag :type="row.timeoutFlag ? 'danger' : 'success'">
              {{ row.timeoutFlag ? '已超时' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text @click="router.push(`/tickets/${row.ticketId}`)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>
