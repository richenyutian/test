package com.example.ticketworkflow.service;

import com.example.ticketworkflow.common.BusinessException;
import com.example.ticketworkflow.model.FormFieldDefinition;
import com.example.ticketworkflow.model.WorkflowNodeDefinition;
import com.example.ticketworkflow.model.WorkflowTemplateDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WorkflowPayloadService {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public WorkflowPayloadService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("JSON序列化失败: " + ex.getMessage());
        }
    }

    public WorkflowTemplateDefinition parseDefinition(String json) {
        try {
            return objectMapper.readValue(json, WorkflowTemplateDefinition.class);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("流程模板解析失败: " + ex.getMessage());
        }
    }

    public Map<String, Object> parseMap(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("表单数据解析失败: " + ex.getMessage());
        }
    }

    public Map<String, Object> mergeFormData(Map<String, Object> existing, Map<String, Object> incoming) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (existing != null) {
            merged.putAll(existing);
        }
        if (incoming != null) {
            incoming.forEach((key, value) -> {
                if (value != null) {
                    merged.put(key, value);
                }
            });
        }
        return merged;
    }

    public void validateRequiredFields(List<FormFieldDefinition> formFields, Map<String, Object> formData, String scopeName) {
        List<String> missingLabels = new ArrayList<>();
        if (formFields == null) {
            return;
        }
        Map<String, Object> values = formData == null ? Map.of() : formData;
        for (FormFieldDefinition field : formFields) {
            if (!field.required()) {
                continue;
            }
            Object value = values.get(field.key());
            if (isEmptyValue(value)) {
                missingLabels.add(field.label());
            }
        }
        if (!missingLabels.isEmpty()) {
            throw new BusinessException(scopeName + "缺少必填字段: " + String.join("、", missingLabels));
        }
    }

    public Map<String, String> buildFieldLabelMap(WorkflowTemplateDefinition definition) {
        Map<String, String> labelMap = new LinkedHashMap<>();
        if (definition.startFormFields() != null) {
            definition.startFormFields().forEach(field -> labelMap.put(field.key(), field.label()));
        }
        if (definition.nodes() != null) {
            for (WorkflowNodeDefinition node : definition.nodes()) {
                if (node.formFields() == null) {
                    continue;
                }
                node.formFields().forEach(field -> labelMap.put(field.key(), field.label()));
            }
        }
        return labelMap;
    }

    public String buildSearchableText(String title, Map<String, Object> mergedFormData) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(title)) {
            parts.add(title);
        }
        if (mergedFormData != null) {
            mergedFormData.forEach((key, value) -> {
                parts.add(key);
                if (value != null) {
                    parts.add(String.valueOf(value));
                }
            });
        }
        return parts.stream()
            .filter(StringUtils::hasText)
            .collect(Collectors.joining(" | "));
    }

    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String text) {
            return !StringUtils.hasText(text);
        }
        if (value instanceof List<?> list) {
            return list.isEmpty();
        }
        return false;
    }
}
