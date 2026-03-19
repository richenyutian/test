<script setup lang="ts">
import { useTicketWorkbenchStore } from '../stores/ticketWorkbenchStore'

const store = useTicketWorkbenchStore()
</script>

<template>
  <div class="page-stack">
    <div class="content-grid">
      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">SLA 规则</div>
              <div class="panel-desc">支持按分类、优先级和升级策略定义响应/解决时限</div>
            </div>
          </div>
        </template>

        <el-table :data="store.slaRules" border>
          <el-table-column prop="ruleCode" label="规则编码" min-width="140" />
          <el-table-column prop="ruleName" label="规则名称" min-width="180" />
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column prop="responseMinutes" label="响应时限(分)" width="120" />
          <el-table-column prop="resolveMinutes" label="解决时限(分)" width="120" />
          <el-table-column label="自动升级" width="100">
            <template #default="{ row }">
              <el-tag :type="row.autoEscalate ? 'danger' : 'info'">
                {{ row.autoEscalate ? '开启' : '关闭' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">SLA 预警</div>
              <div class="panel-desc">展示即将超时与已超时工单，供主管介入</div>
            </div>
          </div>
        </template>

        <div class="record-list">
          <div
            v-for="alert in store.slaAlerts"
            :key="alert.ticketNo"
            class="record-card"
          >
            <div class="record-title">{{ alert.ticketNo }} / {{ alert.title }}</div>
            <div class="record-time">
              {{ alert.alertType }} · {{ alert.ownerName }} · {{ alert.teamName }} · {{ alert.remainingTime }}
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>
