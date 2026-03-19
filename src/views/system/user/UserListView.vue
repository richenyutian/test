<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import { createUserApi, getUserPageApi, updateUserApi } from '../../../api/modules/user'
import { getRoleOptionsApi } from '../../../api/modules/role'
import { getGroupOptionsApi } from '../../../api/modules/group'
import type { OptionItem, PageResponse } from '../../../types/api'
import type { UserPageItem, UserSaveRequest } from '../../../types/system'
import { formatDateTime } from '../../../utils/date'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()

const page = reactive<PageResponse<UserPageItem>>({
  current: 1,
  size: 10,
  total: 0,
  records: [],
})

const keyword = ref('')
const roleOptions = ref<OptionItem[]>([])
const groupOptions = ref<OptionItem[]>([])

const form = reactive<UserSaveRequest>({
  username: '',
  displayName: '',
  userType: 'INTERNAL',
  externalUserFlag: 0,
  phone: '',
  email: '',
  status: 'ENABLED',
  remark: '',
  roleIds: [],
  primaryHandleGroupId: undefined,
})

const loadOptions = async () => {
  roleOptions.value = await getRoleOptionsApi()
  groupOptions.value = await getGroupOptionsApi()
}

const loadPage = async () => {
  loading.value = true
  try {
    const data = await getUserPageApi({
      current: page.current,
      size: page.size,
      keyword: keyword.value || undefined,
    })
    Object.assign(page, data)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = undefined
  Object.assign(form, {
    username: '',
    displayName: '',
    userType: 'INTERNAL',
    externalUserFlag: 0,
    phone: '',
    email: '',
    status: 'ENABLED',
    remark: '',
    roleIds: [],
    primaryHandleGroupId: undefined,
  })
  dialogVisible.value = true
}

const openEdit = (row: UserPageItem) => {
  editingId.value = row.userId
  Object.assign(form, {
    username: row.username,
    displayName: row.displayName,
    userType: row.userType,
    externalUserFlag: row.externalUserFlag,
    phone: row.phone || '',
    email: row.email || '',
    status: row.status,
    remark: row.remark || '',
    roleIds: [],
    primaryHandleGroupId: groupOptions.value.find((item) => item.label === row.primaryHandleGroupName)?.value,
  })
  dialogVisible.value = true
}

const submit = async () => {
  if (editingId.value) {
    await updateUserApi(editingId.value, form)
  } else {
    await createUserApi(form)
  }
  dialogVisible.value = false
  await loadPage()
}

onMounted(async () => {
  await loadOptions()
  await loadPage()
})
</script>

<template>
  <div class="page-container">
    <el-card class="module-card">
      <template #header>
        <div class="toolbar">
          <div class="toolbar__left">
            <el-input v-model="keyword" placeholder="搜索用户名或姓名" clearable style="width: 260px" />
            <el-button type="primary" @click="loadPage">查询</el-button>
          </div>
          <el-button v-permission="'sys:user:create'" type="primary" @click="openCreate">
            新增用户
          </el-button>
        </div>
      </template>

      <el-table :data="page.records" border v-loading="loading">
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="displayName" label="姓名" min-width="120" />
        <el-table-column prop="userType" label="用户类型" width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="roles" label="角色" min-width="180">
          <template #default="{ row }">
            <div class="tag-list">
              <el-tag v-for="item in row.roles" :key="item" effect="plain">{{ item }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="primaryHandleGroupName" label="处理组" min-width="140" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'sys:user:update'" type="primary" text @click="openEdit(row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          layout="total, prev, pager, next"
          @current-change="loadPage"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="720px">
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="form.username" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="form.displayName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户类型">
              <el-select v-model="form.userType" style="width: 100%">
                <el-option label="内部用户" value="INTERNAL" />
                <el-option label="外部用户" value="EXTERNAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="外部用户标识">
              <el-switch v-model="form.externalUserFlag" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select v-model="form.roleIds" multiple style="width: 100%">
                <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理组">
              <el-select v-model="form.primaryHandleGroupId" clearable style="width: 100%">
                <el-option v-for="item in groupOptions" :key="item.value" :label="item.label" :value="item.value" />
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
