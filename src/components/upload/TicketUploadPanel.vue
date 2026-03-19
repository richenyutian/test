<script setup lang="ts">
import { Download, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { downloadTicketAttachmentApi, uploadTicketAttachmentApi } from '../../api/modules/ticket'
import type { TicketAttachment } from '../../types/ticket'
import { formatDateTime, formatFileSize } from '../../utils/date'

const props = defineProps<{
  ticketId: number
  attachments: TicketAttachment[]
  readonly?: boolean
}>()

const emit = defineEmits<{
  uploaded: []
}>()

const handleUpload = async (options: any) => {
  try {
    await uploadTicketAttachmentApi(props.ticketId, options.file)
    ElMessage.success('附件上传成功')
    emit('uploaded')
    options.onSuccess()
  } catch (error) {
    options.onError(error)
  }
}

const handleDownload = async (attachment: TicketAttachment) => {
  const blob = await downloadTicketAttachmentApi(attachment.attachmentId)
  const url = window.URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = attachment.originalFileName
  anchor.click()
  window.URL.revokeObjectURL(url)
}
</script>

<template>
  <div class="panel-card inner-panel">
    <div class="panel-header" style="margin-bottom: 12px">
      <div>
        <div class="panel-title">工单附件</div>
        <div class="panel-desc">支持本地文件上传、列表展示与下载</div>
      </div>

      <el-upload
        v-if="!readonly"
        :show-file-list="false"
        :http-request="handleUpload"
      >
        <el-button type="primary" :icon="Upload">上传附件</el-button>
      </el-upload>
    </div>

    <el-empty v-if="!attachments.length" description="暂无附件" />

    <div v-else class="record-list">
      <div
        v-for="item in attachments"
        :key="item.attachmentId"
        class="record-card"
      >
        <div class="record-title">{{ item.originalFileName }}</div>
        <div class="record-time">
          {{ item.uploaderName }} · {{ formatFileSize(item.fileSize) }} · {{ formatDateTime(item.createdAt) }}
        </div>
        <div class="header-actions" style="margin-top: 10px">
          <el-button type="primary" text :icon="Download" @click="handleDownload(item)">
            下载
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>
