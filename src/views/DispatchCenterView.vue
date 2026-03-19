<script setup lang="ts">
import { useTicketWorkbenchStore } from '../stores/ticketWorkbenchStore'
import { formatDateTime } from '../utils/ticketSystem'

const store = useTicketWorkbenchStore()
</script>

<template>
  <div class="page-stack">
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-label">待分派工单</div>
        <div class="stat-value">{{ store.pendingDispatchTickets.length }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-label">升级工单</div>
        <div class="stat-value">{{ store.tickets.filter((item) => item.escalated).length }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-label">需主管干预</div>
        <div class="stat-value">{{ store.tickets.filter((item) => item.timeout).length }}</div>
      </el-card>
    </div>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div>
            <div class="panel-title">分派中心</div>
            <div class="panel-desc">支持客服和主管查看待分派、重开和升级工单</div>
          </div>
        </div>
      </template>

      <el-table :data="store.pendingDispatchTickets" border>
        <el-table-column prop="ticketNo" label="工单编号" min-width="160" />
        <el-table-column prop="title" label="标题" min-width="260" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="priority" label="优先级" width="90" />
        <el-table-column prop="currentGroupName" label="建议处理组" width="140" />
        <el-table-column prop="currentAssigneeName" label="建议处理人" width="120" />
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
