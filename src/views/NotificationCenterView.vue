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
            <div class="panel-title">通知中心</div>
            <div class="panel-desc">覆盖站内消息与邮件通知，用于工单受理、超时预警和待确认提醒</div>
          </div>
          <el-tag type="danger">未读 {{ store.unreadNotificationCount }}</el-tag>
        </div>
      </template>

      <div class="record-list">
        <div
          v-for="item in store.notifications"
          :key="item.messageId"
          class="record-card"
        >
          <div class="template-card__header">
            <div class="record-title">{{ item.title }}</div>
            <el-tag :type="item.read ? 'info' : 'danger'">
              {{ item.read ? '已读' : '未读' }}
            </el-tag>
          </div>
          <div class="record-time">{{ item.channel }} / {{ item.receiverName }} / {{ formatDateTime(item.sentAt) }}</div>
          <div class="muted-text" style="margin-top: 8px">{{ item.content }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>
