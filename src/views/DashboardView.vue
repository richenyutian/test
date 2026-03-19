<script setup lang="ts">
import { computed } from 'vue'

import { useTicketWorkbenchStore } from '../stores/ticketWorkbenchStore'
import { formatDateTime, statusLabelMap } from '../utils/ticketSystem'

const store = useTicketWorkbenchStore()

const latestTickets = computed(() => store.tickets.slice(0, 8))
</script>

<template>
  <div class="page-stack">
    <div class="stats-grid">
      <el-card
        v-for="item in store.dashboardMetrics"
        :key="item.label"
        class="stat-card"
      >
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ item.value }}</div>
        <div class="muted-text">{{ item.trend }}</div>
      </el-card>
    </div>

    <div class="content-grid">
      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">近 7 日工单趋势</div>
              <div class="panel-desc">用于验证工单量趋势报表与峰值时段判断</div>
            </div>
          </div>
        </template>

        <div class="record-list">
          <div
            v-for="point in store.ticketTrend"
            :key="point.date"
            class="record-card"
          >
            <div class="chip-row" style="justify-content: space-between">
              <span>{{ point.date }}</span>
              <strong>{{ point.count }} 单</strong>
            </div>
            <el-progress
              :percentage="Math.min(100, point.count * 6)"
              :show-text="false"
              style="margin-top: 10px"
            />
          </div>
        </div>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">状态分布</div>
              <div class="panel-desc">覆盖待受理、处理中、待确认、挂起等状态</div>
            </div>
          </div>
        </template>

        <div class="record-list">
          <div
            v-for="item in store.statusStatistics"
            :key="item.name"
            class="record-card"
          >
            <div class="chip-row" style="justify-content: space-between">
              <span>{{ statusLabelMap[item.name as keyof typeof statusLabelMap] || item.name }}</span>
              <strong>{{ item.count }}</strong>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div>
            <div class="panel-title">最新工单</div>
            <div class="panel-desc">用于工作台快速跟踪最新创建和更新的工单</div>
          </div>
        </div>
      </template>

      <el-table :data="latestTickets" border>
        <el-table-column prop="ticketNo" label="工单编号" min-width="160" />
        <el-table-column prop="title" label="标题" min-width="240" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="currentAssigneeName" label="当前处理人" width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            {{ statusLabelMap[row.status] }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
