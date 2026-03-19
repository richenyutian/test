<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { createSlaRuleApi, getSlaOverviewApi, scanSlaApi, updateSlaRuleApi } from '../../api/modules/sla'
import type { SlaOverview, SlaRuleSaveRequest } from '../../types/sla'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const overview = ref<SlaOverview>()

const form = reactive<SlaRuleSaveRequest>({
  priorityCode: 'P2',
  responseLimitMinutes: 30,
  resolveLimitMinutes: 240,
  enabledFlag: 1,
  remark: '',
})

const loadData = async () => {
  loading.value = true
  try {
    overview.value = await getSlaOverviewApi()
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = undefined
  Object.assign(form, {
    priorityCode: 'P2',
    responseLimitMinutes: 30,
    resolveLimitMinutes: 240,
    enabledFlag: 1,
    remark: '',
  })
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  editingId.value = row.slaRuleId
  Object.assign(form, {
    priorityCode: row.priorityCode,
    responseLimitMinutes: row.responseLimitMinutes,
    resolveLimitMinutes: row.resolveLimitMinutes,
    enabledFlag: row.enabledFlag,
    remark: row.remark,
  })
  dialogVisible.value = true
}

const submit = async () => {
  if (editingId.value) {
    await updateSlaRuleApi(editingId.value, form)
  } else {
    await createSlaRuleApi(form)
  }
  dialogVisible.value = false
  await loadData()
}

const scan = async () => {
  const result = await scanSlaApi()
  ElMessage.success(`扫描完成：响应超时 ${result.responseTimeoutCount}，解决超时 ${result.resolveTimeoutCount}`)
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="grid-two">
      <el-card class="module-card">
        <template #header>
          <div class="toolbar">
            <div class="module-card__title">SLA 规则</div>
            <div class="toolbar__left">
              <el-button v-permission="'ticket:sla:update'" type="primary" @click="openCreate">新增规则</el-button>
              <el-button v-permission="'ticket:sla:view'" @click="scan">立即扫描</el-button>
            </div>
          </div>
        </template>

        <el-table :data="overview?.rules || []" border v-loading="loading">
          <el-table-column prop="priorityCode" label="优先级" width="100" />
          <el-table-column prop="responseLimitMinutes" label="响应时限(分钟)" width="140" />
          <el-table-column prop="resolveLimitMinutes" label="解决时限(分钟)" width="140" />
          <el-table-column label="启用" width="100">
            <template #default="{ row }">
              <el-tag :type="row.enabledFlag ? 'success' : 'info'">{{ row.enabledFlag ? '启用' : '禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="180" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-permission="'ticket:sla:update'" type="primary" text @click="openEdit(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="module-card">
        <template #header>
          <div class="module-card__title">SLA 预警</div>
        </template>

        <div class="record-list">
          <div v-for="item in overview?.alerts || []" :key="item.ticketId" class="record-card">
            <div class="record-title">{{ item.ticketNo }} / {{ item.title }}</div>
            <div class="record-time">{{ item.alertType }} · {{ item.ownerName }} · {{ item.teamName }} · {{ item.remainingTime }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑规则' : '新增规则'" width="640px">
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="form.priorityCode" style="width: 100%">
                <el-option label="P1" value="P1" />
                <el-option label="P2" value="P2" />
                <el-option label="P3" value="P3" />
                <el-option label="P4" value="P4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.enabledFlag" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="响应时限(分钟)">
              <el-input-number v-model="form.responseLimitMinutes" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="解决时限(分钟)">
              <el-input-number v-model="form.resolveLimitMinutes" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="4" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
