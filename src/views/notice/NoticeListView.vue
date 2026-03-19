<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { getNoticeCenterApi, markNoticeReadApi } from '../../api/modules/notice'
import type { NoticeCenter } from '../../types/notice'
import { formatDateTime } from '../../utils/date'

const loading = ref(false)
const noticeCenter = ref<NoticeCenter>()

const loadData = async () => {
  loading.value = true
  try {
    noticeCenter.value = await getNoticeCenterApi()
  } finally {
    loading.value = false
  }
}

const markRead = async (noticeId: number) => {
  await markNoticeReadApi(noticeId)
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <el-card class="module-card" v-loading="loading">
      <template #header>
        <div class="toolbar">
          <div class="module-card__title">站内消息</div>
          <el-tag type="danger">未读 {{ noticeCenter?.unreadCount || 0 }}</el-tag>
        </div>
      </template>

      <div class="record-list">
        <div v-for="item in noticeCenter?.messages || []" :key="item.noticeId" class="record-card">
          <div class="toolbar">
            <div>
              <div class="record-title">{{ item.noticeTitle }}</div>
              <div class="record-time">{{ item.noticeType }} · {{ formatDateTime(item.createdAt) }}</div>
            </div>
            <el-button v-if="!item.readFlag" type="primary" text @click="markRead(item.noticeId)">
              标记已读
            </el-button>
          </div>
          <div class="detail-block small">{{ item.noticeContent }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>
