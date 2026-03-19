<script setup lang="ts">
import { computed } from 'vue'

import { useTicketWorkbenchStore } from '../stores/ticketWorkbenchStore'
import { statusLabelMap } from '../utils/ticketSystem'

const store = useTicketWorkbenchStore()

const reportSummary = computed(() => [
  { label: '平均响应时长', value: '38 分钟' },
  { label: '平均解决时长', value: '5.2 小时' },
  { label: 'SLA 达成率', value: '91.4%' },
  { label: '用户满意度', value: '4.6 / 5' },
])

const getStatusLabel = (status: keyof typeof statusLabelMap) => statusLabelMap[status]
</script>

<template>
  <div class="page-stack">
    <div class="stats-grid">
      <el-card v-for="item in reportSummary" :key="item.label" class="stat-card">
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value" style="font-size: 24px">{{ item.value }}</div>
      </el-card>
    </div>

    <div class="content-grid">
      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">按状态统计</div>
              <div class="panel-desc">验证生命周期状态覆盖与队列结构</div>
            </div>
          </div>
        </template>
        <el-table :data="store.statusStatistics" border>
          <el-table-column label="状态">
            <template #default="{ row }">{{ getStatusLabel(row.name as keyof typeof statusLabelMap) || row.name }}</template>
          </el-table-column>
          <el-table-column prop="count" label="数量" width="100" />
        </el-table>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">按分类统计</div>
              <div class="panel-desc">验证工单类型/分类统计报表</div>
            </div>
          </div>
        </template>
        <el-table :data="store.categoryStatistics" border>
          <el-table-column prop="name" label="分类" />
          <el-table-column prop="count" label="数量" width="100" />
        </el-table>
      </el-card>
    </div>

    <div class="content-grid">
      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">按人员处理量统计</div>
              <div class="panel-desc">用于绩效分析和负载均衡</div>
            </div>
          </div>
        </template>
        <el-table :data="store.assigneeStatistics" border>
          <el-table-column prop="name" label="人员" />
          <el-table-column prop="count" label="处理量" width="100" />
        </el-table>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">按部门处理量统计</div>
              <div class="panel-desc">用于部门效率和资源投入分析</div>
            </div>
          </div>
        </template>
        <el-table :data="store.departmentStatistics" border>
          <el-table-column prop="name" label="部门" />
          <el-table-column prop="count" label="处理量" width="100" />
        </el-table>
      </el-card>
    </div>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div>
            <div class="panel-title">满意度统计</div>
            <div class="panel-desc">用于用户满意度分析与服务质量改进</div>
          </div>
        </div>
      </template>

      <el-table :data="store.satisfactionStatistics" border>
        <el-table-column prop="name" label="满意度等级" />
        <el-table-column prop="count" label="数量" width="100" />
      </el-table>
    </el-card>
  </div>
</template>
