<script setup>
const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:modelValue'])

function cloneFields() {
  return (props.modelValue || []).map((item) => ({ ...item }))
}

function addField() {
  const next = cloneFields()
  next.push({
    key: '',
    label: '',
    type: 'input',
    required: false,
    placeholder: '',
    optionsText: '',
  })
  emit('update:modelValue', next)
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
}
</script>

<template>
  <div class="field-designer">
    <div class="section-toolbar">
      <span>字段设计</span>
      <el-button type="primary" link @click="addField">新增字段</el-button>
    </div>
    <el-empty v-if="!modelValue.length" description="暂无字段" />
    <div v-else class="field-list">
      <el-card v-for="(field, index) in modelValue" :key="index" shadow="never" class="field-card">
        <el-row :gutter="12">
          <el-col :span="5">
            <el-input
              :model-value="field.key"
              placeholder="字段编码"
              @update:model-value="(value) => updateField(index, { key: value })"
            />
          </el-col>
          <el-col :span="5">
            <el-input
              :model-value="field.label"
              placeholder="字段名称"
              @update:model-value="(value) => updateField(index, { label: value })"
            />
          </el-col>
          <el-col :span="4">
            <el-select
              style="width: 100%"
              :model-value="field.type"
              @update:model-value="(value) => updateField(index, { type: value })"
            >
              <el-option label="单行文本" value="input" />
              <el-option label="多行文本" value="textarea" />
              <el-option label="数字" value="number" />
              <el-option label="下拉框" value="select" />
              <el-option label="日期" value="date" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-switch
              :model-value="field.required"
              inline-prompt
              active-text="必填"
              inactive-text="可选"
              @update:model-value="(value) => updateField(index, { required: value })"
            />
          </el-col>
          <el-col :span="6" class="align-right">
            <el-button type="danger" link @click="removeField(index)">删除字段</el-button>
          </el-col>
        </el-row>
        <el-row :gutter="12" class="field-card-row">
          <el-col :span="12">
            <el-input
              :model-value="field.placeholder"
              placeholder="占位提示"
              @update:model-value="(value) => updateField(index, { placeholder: value })"
            />
          </el-col>
          <el-col :span="12">
            <el-input
              :model-value="field.optionsText"
              placeholder="下拉选项，逗号分隔"
              @update:model-value="(value) => updateField(index, { optionsText: value })"
            />
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>
