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
              <div class="panel-title">流程状态流转规则</div>
              <div class="panel-desc">明确哪些角色可以执行哪些状态变更</div>
            </div>
          </div>
        </template>

        <el-table :data="store.lifecycleRules" border>
          <el-table-column prop="currentStatus" label="当前状态" min-width="120" />
          <el-table-column prop="actionName" label="动作" width="120" />
          <el-table-column prop="targetStatus" label="目标状态" min-width="120" />
          <el-table-column label="允许角色" min-width="220">
            <template #default="{ row }">
              <div class="chip-row">
                <el-tag v-for="role in row.allowedRoles" :key="role" effect="plain">{{ role }}</el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">角色权限矩阵</div>
              <div class="panel-desc">覆盖菜单权限、按钮权限与数据权限</div>
            </div>
          </div>
        </template>

        <div class="record-list">
          <div
            v-for="role in store.rolePermissions"
            :key="role.roleCode"
            class="record-card"
          >
            <div class="record-title">{{ role.roleName }} / {{ role.roleCode }}</div>
            <div class="record-time">数据权限：{{ role.dataScope }}</div>
            <div class="field-help">菜单：{{ role.menuPermissions.join('、') }}</div>
            <div class="field-help">按钮：{{ role.buttonPermissions.join('、') }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>
