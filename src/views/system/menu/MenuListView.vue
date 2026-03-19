<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

import { createMenuApi, getMenuOptionsApi, getMenuTreeApi, updateMenuApi } from '../../../api/modules/menu'
import type { MenuItem, MenuSaveRequest, SelectOption } from '../../../types/system'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const menus = ref<MenuItem[]>([])
const menuOptions = ref<SelectOption[]>([])

const form = reactive<MenuSaveRequest>({
  parentId: 0,
  menuName: '',
  menuType: 'MENU',
  routePath: '',
  componentPath: '',
  permissionCode: '',
  icon: '',
  sortOrder: 1,
  visibleFlag: 1,
  enabledFlag: 1,
  keepAliveFlag: 0,
  remark: '',
})

const loadData = async () => {
  loading.value = true
  try {
    menus.value = await getMenuTreeApi()
    menuOptions.value = await getMenuOptionsApi()
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = undefined
  Object.assign(form, {
    parentId: 0,
    menuName: '',
    menuType: 'MENU',
    routePath: '',
    componentPath: '',
    permissionCode: '',
    icon: '',
    sortOrder: 1,
    visibleFlag: 1,
    enabledFlag: 1,
    keepAliveFlag: 0,
    remark: '',
  })
  dialogVisible.value = true
}

const openEdit = (row: MenuItem) => {
  editingId.value = row.menuId
  Object.assign(form, {
    parentId: row.parentId,
    menuName: row.menuName,
    menuType: row.menuType,
    routePath: row.routePath,
    componentPath: row.componentPath,
    permissionCode: row.permissionCode,
    icon: row.icon,
    sortOrder: row.sortOrder,
    visibleFlag: row.visibleFlag,
    enabledFlag: row.enabledFlag,
    keepAliveFlag: row.keepAliveFlag,
    remark: row.remark,
  })
  dialogVisible.value = true
}

const submit = async () => {
  if (editingId.value) {
    await updateMenuApi(editingId.value, form)
  } else {
    await createMenuApi(form)
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
          <div class="toolbar__title">菜单管理</div>
          <el-button v-permission="'sys:menu:create'" type="primary" @click="openCreate">新增菜单</el-button>
        </div>
      </template>

      <el-table :data="menus" row-key="menuId" border default-expand-all v-loading="loading">
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column prop="menuType" label="类型" width="100" />
        <el-table-column prop="routePath" label="路由" min-width="160" />
        <el-table-column prop="componentPath" label="组件路径" min-width="220" />
        <el-table-column prop="permissionCode" label="权限标识" min-width="180" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-permission="'sys:menu:update'" type="primary" text @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑菜单' : '新增菜单'" width="760px">
      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上级菜单">
              <el-select v-model="form.parentId" style="width: 100%">
                <el-option :value="0" label="根节点" />
                <el-option v-for="item in menuOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单类型">
              <el-select v-model="form.menuType" style="width: 100%">
                <el-option label="菜单" value="MENU" />
                <el-option label="按钮" value="BUTTON" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单名称">
              <el-input v-model="form.menuName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由路径">
              <el-input v-model="form.routePath" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件路径">
              <el-input v-model="form.componentPath" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限标识">
              <el-input v-model="form.permissionCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标">
              <el-input v-model="form.icon" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="可见">
              <el-switch v-model="form.visibleFlag" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="启用">
              <el-switch v-model="form.enabledFlag" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="缓存">
              <el-switch v-model="form.keepAliveFlag" :active-value="1" :inactive-value="0" />
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
