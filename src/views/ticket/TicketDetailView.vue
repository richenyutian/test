<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import { getGroupOptionsApi } from '../../api/modules/group'
import { getUserOptionsApi } from '../../api/modules/user'
import { getTicketDetailApi } from '../../api/modules/ticket'
import StatusTag from '../../components/common/StatusTag.vue'
import TicketActionDrawer from '../../components/ticket/TicketActionDrawer.vue'
import TicketUploadPanel from '../../components/upload/TicketUploadPanel.vue'
import type { SelectOption } from '../../types/system'
import type { TicketDetail } from '../../types/ticket'
import { formatDateTime } from '../../utils/date'
import { categoryLabelMap, priorityLabelMap, sourceLabelMap, ticketTypeLabelMap } from '../../utils/dict'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const detail = ref<TicketDetail>()
const actionVisible = ref(false)
const currentAction = ref('')
const groupOptions = ref<SelectOption[]>([])
const userOptions = ref<SelectOption[]>([])

const ticketId = computed(() => Number(route.params.ticketId))

const loadData = async () => {
  loading.value = true
  try {
    detail.value = await getTicketDetailApi(ticketId.value)
  } finally {
    loading.value = false
  }
}

const openAction = (action: string) => {
  currentAction.value = action
  actionVisible.value = true
}

const mapActionLabel = (action: string) => ({
  ACCEPT: '受理',
  ASSIGN: '分派',
  PROCESS: '接单',
  TRANSFER: '转派',
  SUSPEND: '挂起',
  RESUME: '恢复',
  SUBMIT_SOLUTION: '完成',
  CONFIRM: '确认',
  CLOSE: '关闭',
  REOPEN: '重开',
  ESCALATE: '升级',
}[action] || action)

const mapActionPermission = (action: string) => ({
  ACCEPT: 'ticket:accept',
  ASSIGN: 'ticket:assign',
  PROCESS: 'ticket:process',
  TRANSFER: 'ticket:transfer',
  SUSPEND: 'ticket:suspend',
  RESUME: 'ticket:suspend',
  SUBMIT_SOLUTION: 'ticket:finish',
  CLOSE: 'ticket:close',
  REOPEN: 'ticket:reopen',
  ESCALATE: 'ticket:escalate',
}[action])

onMounted(async () => {
  ;[groupOptions.value, userOptions.value] = await Promise.all([
    getGroupOptionsApi(),
    getUserOptionsApi(),
  ])
  await loadData()
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <template v-if="detail">
      <el-card class="module-card">
        <template #header>
          <div class="toolbar">
            <div>
              <div class="module-card__title">{{ detail.ticketNo }} - {{ detail.title }}</div>
              <div class="module-card__desc">当前状态：{{ detail.currentStatus }}</div>
            </div>
            <div class="toolbar__left">
              <StatusTag :status="detail.currentStatus" />
              <el-button
                v-for="action in detail.allowedActions"
                :key="action"
                v-permission="action === 'CONFIRM' ? ['ticket:create', 'ticket:reopen'] : mapActionPermission(action)"
                type="primary"
                plain
                @click="openAction(action)"
              >
                {{ mapActionLabel(action) }}
              </el-button>
            </div>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="提单人">{{ detail.requesterName }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ detail.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ sourceLabelMap[detail.sourceCode] || detail.sourceCode }}</el-descriptions-item>
          <el-descriptions-item label="工单类型">{{ ticketTypeLabelMap[detail.ticketTypeCode] || detail.ticketTypeCode }}</el-descriptions-item>
          <el-descriptions-item label="工单分类">{{ categoryLabelMap[detail.categoryCode] || detail.categoryCode }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ priorityLabelMap[detail.priorityCode] || detail.priorityCode }}</el-descriptions-item>
          <el-descriptions-item label="当前处理人">{{ detail.currentHandlerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前处理组">{{ detail.currentHandleGroupName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="响应时限">{{ formatDateTime(detail.responseDeadline) }}</el-descriptions-item>
          <el-descriptions-item label="解决时限">{{ formatDateTime(detail.resolveDeadline) }}</el-descriptions-item>
          <el-descriptions-item label="是否超时">{{ detail.timeoutFlag ? '是' : '否' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">问题描述</el-divider>
        <div class="detail-block">{{ detail.description }}</div>

        <el-divider content-position="left">处理信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="处理结果">{{ detail.resolutionSummary || '-' }}</el-descriptions-item>
          <el-descriptions-item label="挂起原因">{{ detail.suspendReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关闭原因">{{ detail.closeReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="重开原因">{{ detail.reopenReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="升级原因">{{ detail.escalateReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="升级时间">{{ formatDateTime(detail.escalateTime) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <div class="grid-two">
        <el-card class="module-card">
          <template #header>
            <div class="module-card__title">流转记录</div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="item in detail.flowRecords"
              :key="item.flowRecordId"
              :timestamp="formatDateTime(item.operateTime)"
            >
              <div class="record-title">{{ mapActionLabel(item.actionCode) }} / {{ item.operatorName }}</div>
              <div class="record-time">
                {{ item.fromStatus || '-' }} -> {{ item.toStatus || '-' }}
              </div>
              <div class="detail-block small">{{ item.operateDescription }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <TicketUploadPanel
          :ticket-id="detail.ticketId"
          :attachments="detail.attachments"
          :readonly="!authStore.hasButtonPermission('ticket:create')"
          @uploaded="loadData"
        />
      </div>
    </template>

    <TicketActionDrawer
      v-model="actionVisible"
      :ticket-id="ticketId"
      :action-code="currentAction"
      :group-options="groupOptions"
      :user-options="userOptions"
      @success="loadData"
    />
  </div>
</template>
