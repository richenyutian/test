<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import { createGroupApi, getGroupListApi, updateGroupApi } from '../../../api/modules/group'
import { getUserOptionsApi } from '../../../api/modules/user'
import type { HandleGroupItem, HandleGroupSaveRequest, SelectOption } from '../../../types/system'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const groups = ref<HandleGroupItem[]>([])
const leaderOptions = ref<SelectOption[]>([])

const form = reactive<HandleGroupSaveRequest>({
  groupCode: '',
  groupName: '',
  groupType: 'OPS',
  leaderUserId: undefined,
  leaderName: '',
  status: 'ENABLED',
  remark: '',
})

const loadData = async () => {
  loading.value = true
  try {
    groups.value = await getGroupListApi()
    leaderOptions.value = await getUserOptionsApi()
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = undefined
  Object.assign(form, {
    groupCode: '',
    groupName: '',
    groupType: 'OPS',
    leaderUserId: undefined,
    leaderName: '',
    status: 'ENABLED',
    remark: '',
  })
  dialogVisible.value = true
}

const openEdit = (row: HandleGroupItem) => {
  editingId.value = row.handleGroupId
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const submit = async () => {
  form.leaderName = leaderOptions.value.find((item) => item.value === form.leaderUserId)?.label || ''
  if (editingId.value) {
    await updateGroupApi(editingId.value, form)
  } else {
    await createGroupApi(form)
  }
  dialogVisible.value = false
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <el-card class="module-card">
      <template #header>
        <div class="toolbar">
          <div class="toolbar__title">处理组管理</div>
          <el-button v-permission="'sys:group:create'" type="primary" @click="openCreate">新增处理组</el-button>
        </div>
      </template>

      <el-table :data="groups" border v-loading="loading">
        <el-table-column prop="groupCode" label="处理组编码" min-width="140" />
        <el-table-column prop="groupName" label="处理组名称" min-width="160" />
        <el-table-column prop="groupType" label="类型" width="100" />
        <el-table-column prop="leaderName" label="组长" width="120" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="remark" label="备注" min-width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-permission="'sys:group:update'" type="primary" text @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑处理组' : '新增处理组'" width="720px">
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="处理组编码">
              <el-input v-model="form.groupCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理组名称">
              <el-input v-model="form.groupName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理组类型">
              <el-select v-model="form.groupType" style="width: 100%">
                <el-option label="OPS" value="OPS" />
                <el-option label="RD" value="RD" />
                <el-option label="MANAGEMENT" value="MANAGEMENT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组长">
              <el-select v-model="form.leaderUserId" clearable style="width: 100%">
                <el-option v-for="item in leaderOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="启用" value="ENABLED" />
                <el-option label="禁用" value="DISABLED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="4" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
