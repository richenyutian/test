<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import { getAuditLogPageApi } from '../../api/modules/audit'
import type { AuditLogPage } from '../../types/audit'
import { formatDateTime } from '../../utils/date'

const loading = ref(false)
const page = reactive<AuditLogPage>({
  current: 1,
  size: 20,
  total: 0,
  records: [],
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await getAuditLogPageApi({
      current: page.current,
      size: page.size,
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
        <div class="module-card__title">审计日志</div>
      </template>

      <el-table :data="page.records" border v-loading="loading">
        <el-table-column prop="moduleName" label="模块" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="140" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="businessId" label="业务ID" width="100" />
        <el-table-column prop="operationDescription" label="描述" min-width="220" />
        <el-table-column prop="requestPath" label="请求路径" min-width="200" />
        <el-table-column prop="requestIp" label="IP" width="120" />
        <el-table-column label="时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
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
