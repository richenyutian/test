<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { createTicketApi } from '../../api/modules/ticket'
import type { TicketCreateRequest } from '../../types/ticket'

const router = useRouter()

const form = reactive<TicketCreateRequest>({
  title: '',
  description: '',
  contactPhone: '13800000005',
  externalUserFlag: 1,
  sourceCode: 'USER_SUBMIT',
  ticketTypeCode: 'INCIDENT',
  categoryCode: 'APPLICATION',
  priorityCode: 'P2',
})

const submit = async () => {
  const ticketId = await createTicketApi(form)
  ElMessage.success('工单创建成功')
  router.push(`/tickets/${ticketId}`)
}
</script>

<template>
  <div class="page-container">
    <el-card class="module-card">
      <template #header>
        <div class="module-card__title">创建工单</div>
      </template>

      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工单标题">
              <el-input v-model="form.title" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式">
              <el-input v-model="form.contactPhone" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源">
              <el-select v-model="form.sourceCode" style="width: 100%">
                <el-option label="用户提交" value="USER_SUBMIT" />
                <el-option label="客服录入" value="SERVICE_ENTRY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="工单类型">
              <el-select v-model="form.ticketTypeCode" style="width: 100%">
                <el-option label="故障" value="INCIDENT" />
                <el-option label="需求" value="REQUEST" />
                <el-option label="变更" value="CHANGE" />
                <el-option label="咨询" value="CONSULT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
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
            <el-form-item label="工单分类">
              <el-select v-model="form.categoryCode" style="width: 100%">
                <el-option label="账号权限" value="ACCOUNT" />
                <el-option label="网络链路" value="NETWORK" />
                <el-option label="数据库" value="DATABASE" />
                <el-option label="应用系统" value="APPLICATION" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="外部用户">
              <el-switch v-model="form.externalUserFlag" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="问题描述">
              <el-input v-model="form.description" type="textarea" :rows="8" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="dialog-footer">
        <el-button @click="router.back()">返回</el-button>
        <el-button type="primary" @click="submit">提交工单</el-button>
      </div>
    </el-card>
  </div>
</template>
