<script setup lang="ts">
import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'

import {
  acceptTicketApi,
  assignTicketApi,
  claimTicketApi,
  closeTicketApi,
  completeTicketApi,
  confirmTicketApi,
  escalateTicketApi,
  reopenTicketApi,
  resumeTicketApi,
  suspendTicketApi,
  transferTicketApi,
} from '../../api/modules/ticket'
import type { SelectOption } from '../../types/system'

const props = defineProps<{
  modelValue: boolean
  ticketId: number
  actionCode: string
  groupOptions: SelectOption[]
  userOptions: SelectOption[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const form = reactive({
  priorityCode: 'P2',
  categoryCode: 'APPLICATION',
  handleGroupId: undefined as number | undefined,
  handlerUserId: undefined as number | undefined,
  operateDescription: '',
  suspendReason: '',
  resolutionSummary: '',
  closeReason: '',
  reopenReason: '',
  escalateReason: '',
})

const title = computed(() => ({
  ACCEPT: '受理工单',
  ASSIGN: '分派工单',
  PROCESS: '接单处理',
  TRANSFER: '转派工单',
  SUSPEND: '挂起工单',
  RESUME: '恢复工单',
  SUBMIT_SOLUTION: '完成工单',
  CONFIRM: '确认工单',
  CLOSE: '关闭工单',
  REOPEN: '重开工单',
  ESCALATE: '升级工单',
}[props.actionCode] || '工单处理'))

const submit = async () => {
  switch (props.actionCode) {
    case 'ACCEPT':
      await acceptTicketApi(props.ticketId, {
        priorityCode: form.priorityCode,
        categoryCode: form.categoryCode,
      })
      break
    case 'ASSIGN':
      if (!form.handleGroupId) {
        ElMessage.warning('请选择处理组')
        return
      }
      await assignTicketApi(props.ticketId, {
        handleGroupId: form.handleGroupId,
        handlerUserId: form.handlerUserId,
        operateDescription: form.operateDescription,
      })
      break
    case 'PROCESS':
      await claimTicketApi(props.ticketId)
      break
    case 'TRANSFER':
      if (!form.handleGroupId || !form.operateDescription) {
        ElMessage.warning('请填写完整的转派信息')
        return
      }
      await transferTicketApi(props.ticketId, {
        handleGroupId: form.handleGroupId,
        handlerUserId: form.handlerUserId,
        operateDescription: form.operateDescription,
      })
      break
    case 'SUSPEND':
      if (!form.suspendReason) {
        ElMessage.warning('请填写挂起原因')
        return
      }
      await suspendTicketApi(props.ticketId, { suspendReason: form.suspendReason })
      break
    case 'RESUME':
      await resumeTicketApi(props.ticketId)
      break
    case 'SUBMIT_SOLUTION':
      if (!form.resolutionSummary) {
        ElMessage.warning('请填写处理结果')
        return
      }
      await completeTicketApi(props.ticketId, { resolutionSummary: form.resolutionSummary })
      break
    case 'CONFIRM':
      await confirmTicketApi(props.ticketId)
      break
    case 'CLOSE':
      if (!form.closeReason) {
        ElMessage.warning('请填写关闭原因')
        return
      }
      await closeTicketApi(props.ticketId, { closeReason: form.closeReason })
      break
    case 'REOPEN':
      if (!form.reopenReason) {
        ElMessage.warning('请填写重开原因')
        return
      }
      await reopenTicketApi(props.ticketId, { reopenReason: form.reopenReason })
      break
    case 'ESCALATE':
      if (!form.escalateReason) {
        ElMessage.warning('请填写升级原因')
        return
      }
      await escalateTicketApi(props.ticketId, { escalateReason: form.escalateReason })
      break
    default:
      break
  }

  ElMessage.success('操作成功')
  emit('update:modelValue', false)
  emit('success')
}
</script>

<template>
  <el-drawer
    :model-value="modelValue"
    :title="title"
    size="520px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <template v-if="actionCode === 'ACCEPT'">
        <el-form-item label="优先级">
          <el-select v-model="form.priorityCode" style="width: 100%">
            <el-option label="P1" value="P1" />
            <el-option label="P2" value="P2" />
            <el-option label="P3" value="P3" />
            <el-option label="P4" value="P4" />
          </el-select>
        </el-form-item>
        <el-form-item label="工单分类">
          <el-select v-model="form.categoryCode" style="width: 100%">
            <el-option label="账号权限" value="ACCOUNT" />
            <el-option label="网络链路" value="NETWORK" />
            <el-option label="数据库" value="DATABASE" />
            <el-option label="应用系统" value="APPLICATION" />
          </el-select>
        </el-form-item>
      </template>

      <template v-if="actionCode === 'ASSIGN' || actionCode === 'TRANSFER'">
        <el-form-item label="处理组">
          <el-select v-model="form.handleGroupId" style="width: 100%">
            <el-option v-for="item in groupOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人">
          <el-select v-model="form.handlerUserId" clearable style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.operateDescription" type="textarea" :rows="4" />
        </el-form-item>
      </template>

      <el-form-item v-if="actionCode === 'SUSPEND'" label="挂起原因">
        <el-input v-model="form.suspendReason" type="textarea" :rows="4" />
      </el-form-item>

      <el-form-item v-if="actionCode === 'SUBMIT_SOLUTION'" label="处理结果">
        <el-input v-model="form.resolutionSummary" type="textarea" :rows="5" />
      </el-form-item>

      <el-form-item v-if="actionCode === 'CLOSE'" label="关闭原因">
        <el-input v-model="form.closeReason" type="textarea" :rows="4" />
      </el-form-item>

      <el-form-item v-if="actionCode === 'REOPEN'" label="重开原因">
        <el-input v-model="form.reopenReason" type="textarea" :rows="4" />
      </el-form-item>

      <el-form-item v-if="actionCode === 'ESCALATE'" label="升级原因">
        <el-input v-model="form.escalateReason" type="textarea" :rows="4" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" @click="submit">确认</el-button>
      </div>
    </template>
  </el-drawer>
</template>
