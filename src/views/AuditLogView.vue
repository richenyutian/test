<script setup lang="ts">
import { useTicketWorkbenchStore } from '../stores/ticketWorkbenchStore'
import { formatDateTime } from '../utils/ticketSystem'

const store = useTicketWorkbenchStore()
</script>

<template>
  <div class="page-stack">
    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div>
            <div class="panel-title">审计日志</div>
            <div class="panel-desc">关键操作全链路记录，满足过程可追溯与可审计要求</div>
          </div>
        </div>
      </template>

      <el-table :data="store.auditLogs" border>
        <el-table-column prop="moduleName" label="模块" width="140" />
        <el-table-column prop="operationName" label="操作" width="140" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="requestPath" label="请求路径" min-width="220" />
        <el-table-column prop="result" label="结果" width="100" />
        <el-table-column label="操作时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.operatedAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
