<script setup>
const props = defineProps({
  schema: {
    type: Array,
    default: () => [],
  },
  modelValue: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['update:modelValue'])

function updateField(key, value) {
  emit('update:modelValue', {
    ...props.modelValue,
    [key]: value,
  })
}
</script>

<template>
  <el-form label-position="top" class="dynamic-form">
    <el-row :gutter="16">
      <el-col
        v-for="field in schema"
        :key="field.key"
        :xs="24"
        :md="field.type === 'textarea' ? 24 : 12"
      >
        <el-form-item :label="field.label" :required="field.required">
          <el-input
            v-if="field.type === 'input'"
            :model-value="modelValue[field.key]"
            :placeholder="field.placeholder || `请输入${field.label}`"
            @update:model-value="(value) => updateField(field.key, value)"
          />
          <el-input
            v-else-if="field.type === 'textarea'"
            type="textarea"
            :rows="4"
            :model-value="modelValue[field.key]"
            :placeholder="field.placeholder || `请输入${field.label}`"
            @update:model-value="(value) => updateField(field.key, value)"
          />
          <el-input-number
            v-else-if="field.type === 'number'"
            :model-value="modelValue[field.key]"
            style="width: 100%"
            controls-position="right"
            @update:model-value="(value) => updateField(field.key, value)"
          />
          <el-date-picker
            v-else-if="field.type === 'date'"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :model-value="modelValue[field.key]"
            @update:model-value="(value) => updateField(field.key, value)"
          />
          <el-select
            v-else-if="field.type === 'select'"
            style="width: 100%"
            :model-value="modelValue[field.key]"
            :placeholder="field.placeholder || `请选择${field.label}`"
            @update:model-value="(value) => updateField(field.key, value)"
          >
            <el-option
              v-for="option in field.options || []"
              :key="option"
              :label="option"
              :value="option"
            />
          </el-select>
          <el-input
            v-else
            :model-value="modelValue[field.key]"
            :placeholder="field.placeholder || `请输入${field.label}`"
            @update:model-value="(value) => updateField(field.key, value)"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
