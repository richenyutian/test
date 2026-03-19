<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import { useTicketWorkbenchStore } from '../stores/ticketWorkbenchStore'
import { formatDateTime, sourceLabelMap, statusLabelMap, statusTagTypeMap } from '../utils/ticketSystem'

const route = useRoute()
const store = useTicketWorkbenchStore()

const ticket = computed(() => store.getTicketById(String(route.params.ticketId)))
</script>

<template>
  <div class="page-stack" v-if="ticket">
    <div class="content-grid">
      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">{{ ticket.title }}</div>
              <div class="panel-desc">{{ ticket.ticketNo }}</div>
            </div>
            <el-tag :type="statusTagTypeMap[ticket.status]">{{ statusLabelMap[ticket.status] }}</el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="提单人">{{ ticket.requesterName }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ ticket.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="所属客户">{{ ticket.customerName }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ ticket.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="工单来源">{{ sourceLabelMap[ticket.source] }}</el-descriptions-item>
          <el-descriptions-item label="工单类型">{{ ticket.ticketType }}</el-descriptions-item>
          <el-descriptions-item label="工单分类">{{ ticket.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ ticket.priority }}</el-descriptions-item>
          <el-descriptions-item label="当前处理人">{{ ticket.currentAssigneeName }}</el-descriptions-item>
          <el-descriptions-item label="当前处理组">{{ ticket.currentGroupName }}</el-descriptions-item>
          <el-descriptions-item label="响应时限">{{ formatDateTime(ticket.responseDeadline) }}</el-descriptions-item>
          <el-descriptions-item label="解决时限">{{ formatDateTime(ticket.resolveDeadline) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">问题描述</el-divider>
        <div class="muted-text" style="line-height: 1.8">{{ ticket.description }}</div>

        <el-divider content-position="left">标签</el-divider>
        <div class="chip-row">
          <el-tag v-for="tag in ticket.tags" :key="tag" effect="plain">{{ tag }}</el-tag>
        </div>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">满意度与时效</div>
              <div class="panel-desc">展示用户评价、是否升级和超时标记</div>
            </div>
          </div>
        </template>

        <div class="record-list">
          <div class="record-card">
            <div class="record-title">满意度</div>
            <div class="record-time">{{ ticket.satisfactionLevel || '待评价' }}</div>
          </div>
          <div class="record-card">
            <div class="record-title">评价内容</div>
            <div class="record-time">{{ ticket.satisfactionComment || '暂无评价内容' }}</div>
          </div>
          <div class="record-card">
            <div class="record-title">是否升级</div>
            <div class="record-time">{{ ticket.escalated ? '是' : '否' }}</div>
          </div>
          <div class="record-card">
            <div class="record-title">是否超时</div>
            <div class="record-time">{{ ticket.timeout ? '是' : '否' }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="content-grid">
      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">工单流转记录</div>
              <div class="panel-desc">可审计的全生命周期状态变更轨迹</div>
            </div>
          </div>
        </template>

        <el-timeline>
          <el-timeline-item
            v-for="record in ticket.flowRecords"
            :key="`${record.actionType}-${record.operatedAt}`"
            :timestamp="formatDateTime(record.operatedAt)"
          >
            <div class="record-title">{{ record.actionType }} - {{ record.operatorName }}</div>
            <div class="muted-text">{{ record.remark }}</div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <el-card class="panel-card">
        <template #header>
          <div class="panel-header">
            <div>
              <div class="panel-title">评论 / 沟通记录</div>
              <div class="panel-desc">用于跟踪用户、客服、处理人之间的协作信息</div>
            </div>
          </div>
        </template>

        <div class="record-list">
          <div
            v-for="comment in ticket.comments"
            :key="comment.commentId"
            class="record-card"
          >
            <div class="record-title">{{ comment.authorName }} / {{ comment.authorRole }}</div>
            <div class="record-time">{{ formatDateTime(comment.createdAt) }}</div>
            <div class="muted-text" style="margin-top: 8px">{{ comment.content }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>

  <el-empty v-else description="未找到对应工单" />
</template>
