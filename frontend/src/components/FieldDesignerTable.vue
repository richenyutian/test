<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:modelValue'])

const selectedIndex = ref(0)

const paletteItems = [
  { type: 'input', label: '单行文本', icon: 'Aa' },
  { type: 'textarea', label: '多行文本', icon: 'Tx' },
  { type: 'number', label: '数字', icon: '12' },
  { type: 'select', label: '下拉框', icon: 'Sl' },
  { type: 'date', label: '日期', icon: 'Dt' },
]

const currentField = computed(() => props.modelValue?.[selectedIndex.value] || null)

watch(
  () => props.modelValue.length,
  (length) => {
    if (!length) {
      selectedIndex.value = 0
      return
    }
    if (selectedIndex.value > length - 1) {
      selectedIndex.value = length - 1
    }
  },
)

function cloneFields() {
  return (props.modelValue || []).map((item) => ({ ...item }))
}

function getDefaultField(type) {
  const count = (props.modelValue?.length || 0) + 1
  const map = {
    input: { label: `单行文本${count}`, placeholder: '请输入内容' },
    textarea: { label: `多行文本${count}`, placeholder: '请输入详细内容' },
    number: { label: `数字字段${count}`, placeholder: '请输入数字' },
    select: { label: `下拉字段${count}`, placeholder: '请选择', optionsText: '选项1,选项2' },
    date: { label: `日期字段${count}`, placeholder: '请选择日期' },
  }
  return {
    key: '',
    type,
    required: false,
    optionsText: '',
    ...map[type],
  }
}

function addField(type = 'input') {
  const next = cloneFields()
  next.push(getDefaultField(type))
  emit('update:modelValue', next)
  selectedIndex.value = next.length - 1
}

function updateField(index, patch) {
  const next = cloneFields()
  next[index] = {
    ...next[index],
    ...patch,
  }
  emit('update:modelValue', next)
}

function removeField(index) {
  const next = cloneFields()
  next.splice(index, 1)
  emit('update:modelValue', next)
  if (selectedIndex.value >= next.length) {
    selectedIndex.value = Math.max(next.length - 1, 0)
  }
}

function moveField(index, direction) {
  const target = index + direction
  if (target < 0 || target >= props.modelValue.length) {
    return
  }
  const next = cloneFields()
  const [item] = next.splice(index, 1)
  next.splice(target, 0, item)
  emit('update:modelValue', next)
  selectedIndex.value = target
}

function selectField(index) {
  selectedIndex.value = index
}

function clearFields() {
  emit('update:modelValue', [])
  selectedIndex.value = 0
}

function resolveTypeLabel(type) {
  return paletteItems.find((item) => item.type === type)?.label || type
}
</script>

<template>
  <div class="builder-shell">
    <div class="builder-toolbar">
      <div>
        <div class="builder-title">设计画布</div>
        <div class="builder-desc">参考 vue-form-design playground 的交互方式：左侧组件面板、中间画布、右侧属性配置。</div>
      </div>
      <div class="builder-toolbar-actions">
        <el-button @click="addField()">添加字段</el-button>
        <el-button type="danger" plain @click="clearFields">清空画布</el-button>
      </div>
    </div>

    <div class="builder-grid">
      <el-card shadow="never" class="builder-panel component-panel">
        <template #header>
          <div class="panel-title">组件库</div>
        </template>
        <div class="palette-grid">
          <button
            v-for="item in paletteItems"
            :key="item.type"
            class="palette-item"
            type="button"
            @click="addField(item.type)"
          >
            <span class="palette-icon">{{ item.icon }}</span>
            <span class="palette-label">{{ item.label }}</span>
          </button>
        </div>
      </el-card>

      <el-card shadow="never" class="builder-panel canvas-panel">
        <template #header>
          <div class="panel-title">表单画布</div>
        </template>
        <el-empty v-if="!modelValue.length" description="从左侧选择组件开始搭建表单" />
        <div v-else class="canvas-list">
          <div
            v-for="(field, index) in modelValue"
            :key="`${field.key || field.label}-${index}`"
            class="canvas-item"
            :class="{ active: selectedIndex === index }"
            @click="selectField(index)"
          >
            <div class="canvas-item-main">
              <div class="canvas-item-header">
                <span class="canvas-item-title">{{ field.label || '未命名字段' }}</span>
                <div class="canvas-item-tags">
                  <el-tag size="small" effect="plain">{{ resolveTypeLabel(field.type) }}</el-tag>
                  <el-tag v-if="field.required" size="small" type="danger" effect="light">必填</el-tag>
                </div>
              </div>
              <div class="canvas-item-sub">
                <span>字段标识：{{ field.key || '-' }}</span>
                <span>提示：{{ field.placeholder || '-' }}</span>
              </div>
            </div>
            <div class="canvas-item-actions">
              <el-button link @click.stop="moveField(index, -1)">上移</el-button>
              <el-button link @click.stop="moveField(index, 1)">下移</el-button>
              <el-button link type="danger" @click.stop="removeField(index)">删除</el-button>
            </div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="builder-panel property-panel">
        <template #header>
          <div class="panel-title">字段属性</div>
        </template>
        <el-empty v-if="!currentField" description="请选择画布中的字段进行配置" />
        <template v-else>
          <el-form label-position="top" class="property-form">
            <el-form-item label="字段名称">
              <el-input
                :model-value="currentField.label"
                placeholder="请输入字段名称"
                @update:model-value="(value) => updateField(selectedIndex, { label: value })"
              />
            </el-form-item>
            <el-form-item label="字段编码">
              <el-input
                :model-value="currentField.key"
                placeholder="请输入字段编码"
                @update:model-value="(value) => updateField(selectedIndex, { key: value })"
              />
            </el-form-item>
            <el-form-item label="字段类型">
              <el-select
                style="width: 100%"
                :model-value="currentField.type"
                @update:model-value="(value) => updateField(selectedIndex, { type: value })"
              >
                <el-option v-for="item in paletteItems" :key="item.type" :label="item.label" :value="item.type" />
              </el-select>
            </el-form-item>
            <el-form-item label="占位提示">
              <el-input
                :model-value="currentField.placeholder"
                placeholder="请输入占位提示"
                @update:model-value="(value) => updateField(selectedIndex, { placeholder: value })"
              />
            </el-form-item>
            <el-form-item label="是否必填">
              <el-switch
                :model-value="currentField.required"
                inline-prompt
                active-text="必填"
                inactive-text="可选"
                @update:model-value="(value) => updateField(selectedIndex, { required: value })"
              />
            </el-form-item>
            <el-form-item v-if="currentField.type === 'select'" label="下拉选项">
              <el-input
                :model-value="currentField.optionsText"
                type="textarea"
                :rows="4"
                placeholder="请输入选项，使用逗号分隔"
                @update:model-value="(value) => updateField(selectedIndex, { optionsText: value })"
              />
            </el-form-item>
          </el-form>

          <el-divider>效果预览</el-divider>
          <div class="property-preview">
            <div class="preview-label">
              {{ currentField.label || '字段名称' }}
              <span v-if="currentField.required" class="preview-required">*</span>
            </div>
            <el-input
              v-if="currentField.type === 'input'"
              disabled
              :placeholder="currentField.placeholder || '请输入内容'"
            />
            <el-input
              v-else-if="currentField.type === 'textarea'"
              disabled
              type="textarea"
              :rows="3"
              :placeholder="currentField.placeholder || '请输入内容'"
            />
            <el-input-number
              v-else-if="currentField.type === 'number'"
              disabled
              style="width: 100%"
              controls-position="right"
            />
            <el-select
              v-else-if="currentField.type === 'select'"
              disabled
              style="width: 100%"
              :placeholder="currentField.placeholder || '请选择'"
            >
              <el-option
                v-for="option in (currentField.optionsText || '').split(',').map((item) => item.trim()).filter(Boolean)"
                :key="option"
                :label="option"
                :value="option"
              />
            </el-select>
            <el-date-picker
              v-else-if="currentField.type === 'date'"
              disabled
              type="date"
              style="width: 100%"
            />
            <el-input v-else disabled :placeholder="currentField.placeholder || '请输入内容'" />
          </div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.builder-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.builder-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.builder-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.builder-desc {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.7;
  color: #6b7280;
}

.builder-toolbar-actions {
  display: flex;
  gap: 8px;
}

.builder-grid {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr) 320px;
  gap: 16px;
}

.builder-panel {
  border-radius: 16px !important;
  border: 1px solid #e5e7eb !important;
  background: #fff !important;
}

.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.palette-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.palette-item {
  border: 1px solid #dbeafe;
  background: linear-gradient(180deg, #f8fbff 0%, #eff6ff 100%);
  border-radius: 14px;
  padding: 16px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.palette-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.12);
}

.palette-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: #3b82f6;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
}

.palette-label {
  font-size: 12px;
  color: #334155;
}

.canvas-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.canvas-item {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: #f8fafc;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.canvas-item.active {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.12);
  background: #ffffff;
}

.canvas-item-header,
.canvas-item-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.canvas-item-title {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.canvas-item-tags {
  display: flex;
  gap: 6px;
}

.canvas-item-sub {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
}

.canvas-item-actions {
  margin-top: 10px;
  justify-content: flex-end;
}

.property-form {
  margin-top: 4px;
}

.property-preview {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-label {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
}

.preview-required {
  color: #f56c6c;
  margin-left: 4px;
}
</style>
