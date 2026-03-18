package com.example.ticketsystem.service;

import com.example.ticketsystem.common.BusinessException;
import com.example.ticketsystem.model.FlowGraphDefinition;
import com.example.ticketsystem.model.FormFieldDefinition;
import com.example.ticketsystem.model.FormSchemaDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PayloadService {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public PayloadService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("JSON序列化失败: " + ex.getMessage());
        }
    }

    public FormSchemaDefinition parseFormSchema(String json) {
        try {
            return objectMapper.readValue(json, FormSchemaDefinition.class);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("表单定义解析失败: " + ex.getMessage());
        }
    }

    public FlowGraphDefinition parseFlowGraph(String json) {
        try {
            return objectMapper.readValue(json, FlowGraphDefinition.class);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("流程图解析失败: " + ex.getMessage());
        }
    }

    public Map<String, Object> parseMap(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("业务数据解析失败: " + ex.getMessage());
        }
    }

    public void validateFormData(List<FormFieldDefinition> fields, Map<String, Object> formData, String scopeName) {
        if (fields == null || fields.isEmpty()) {
            return;
        }
        Map<String, Object> values = formData == null ? Map.of() : formData;
        StringBuilder missing = new StringBuilder();
        for (FormFieldDefinition field : fields) {
            if (!field.required()) {
                continue;
            }
            Object value = values.get(field.key());
            if (value == null) {
                appendMissing(missing, field.label());
                continue;
            }
            if (value instanceof String text && !StringUtils.hasText(text)) {
                appendMissing(missing, field.label());
            }
        }
        if (missing.length() > 0) {
            throw new BusinessException(scopeName + "缺少必填字段: " + missing);
        }
    }

    public Map<String, Object> mergeData(Map<String, Object> source, Map<String, Object> incoming) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (source != null) {
            merged.putAll(source);
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

    private void appendMissing(StringBuilder builder, String label) {
        if (builder.length() > 0) {
            builder.append('、');
        }
        builder.append(label);
    }
}
