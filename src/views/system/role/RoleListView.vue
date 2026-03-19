<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import { getRoleListApi, createRoleApi, updateRoleApi } from '../../../api/modules/role'
import { getMenuTreeApi } from '../../../api/modules/menu'
import type { MenuItem, RoleItem, RoleSaveRequest } from '../../../types/system'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const roles = ref<RoleItem[]>([])
const menuTree = ref<MenuItem[]>([])

const form = reactive<RoleSaveRequest>({
  roleCode: '',
  roleName: '',
  dataScope: 'TEAM',
  status: 'ENABLED',
  remark: '',
  menuIds: [],
})

const loadData = async () => {
  loading.value = true
  try {
    roles.value = await getRoleListApi()
    menuTree.value = await getMenuTreeApi()
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = undefined
  Object.assign(form, {
    roleCode: '',
    roleName: '',
    dataScope: 'TEAM',
    status: 'ENABLED',
    remark: '',
    menuIds: [],
  })
  dialogVisible.value = true
}

const openEdit = (row: RoleItem) => {
  editingId.value = row.roleId
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const submit = async () => {
  if (editingId.value) {
    await updateRoleApi(editingId.value, form)
  } else {
    await createRoleApi(form)
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
          <div class="toolbar__title">角色管理</div>
          <el-button v-permission="'sys:role:create'" type="primary" @click="openCreate">新增角色</el-button>
        </div>
      </template>

      <el-table :data="roles" border v-loading="loading">
        <el-table-column prop="roleCode" label="角色编码" min-width="140" />
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="dataScope" label="数据权限" min-width="120" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="remark" label="备注" min-width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-permission="'sys:role:update'" type="primary" text @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="760px">
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="角色编码">
              <el-input v-model="form.roleCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色名称">
              <el-input v-model="form.roleName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据权限">
              <el-select v-model="form.dataScope" style="width: 100%">
                <el-option label="仅本人创建" value="SELF_CREATED" />
                <el-option label="仅本人处理" value="SELF_ASSIGNED" />
                <el-option label="本部门" value="DEPARTMENT" />
                <el-option label="本组" value="TEAM" />
                <el-option label="全部" value="ALL" />
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
            <el-form-item label="关联菜单/按钮权限">
              <el-tree-select
                v-model="form.menuIds"
                :data="menuTree"
                node-key="menuId"
                multiple
                show-checkbox
                check-strictly
                default-expand-all
                style="width: 100%"
                :props="{ label: 'menuName', children: 'children' }"
              />
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
