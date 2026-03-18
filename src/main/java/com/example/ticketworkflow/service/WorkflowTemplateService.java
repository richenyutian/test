package com.example.ticketworkflow.service;

import com.example.ticketworkflow.api.ApiModels;
import com.example.ticketworkflow.common.BusinessException;
import com.example.ticketworkflow.entity.WorkflowTemplateEntity;
import com.example.ticketworkflow.mapper.WorkflowTemplateMapper;
import com.example.ticketworkflow.model.FormFieldDefinition;
import com.example.ticketworkflow.model.WorkflowNodeDefinition;
import com.example.ticketworkflow.model.WorkflowTemplateDefinition;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class WorkflowTemplateService {

    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final RepositoryService repositoryService;
    private final WorkflowPayloadService payloadService;
    private final ProcessModelBuilder processModelBuilder;

    public WorkflowTemplateService(WorkflowTemplateMapper workflowTemplateMapper,
                                   RepositoryService repositoryService,
                                   WorkflowPayloadService payloadService,
                                   ProcessModelBuilder processModelBuilder) {
        this.workflowTemplateMapper = workflowTemplateMapper;
        this.repositoryService = repositoryService;
        this.payloadService = payloadService;
        this.processModelBuilder = processModelBuilder;
    }

    @Transactional
    public ApiModels.WorkflowTemplateView save(ApiModels.TemplateSaveRequest request) {
        WorkflowTemplateEntity existing = request.id() == null ? null : workflowTemplateMapper.findById(request.id());
        if (request.id() != null && existing == null) {
            throw new BusinessException("流程模板不存在");
        }

        String templateCode = StringUtils.hasText(request.templateCode())
            ? normalizeKey(request.templateCode())
            : normalizeKey(request.name());
        WorkflowTemplateEntity sameCode = workflowTemplateMapper.findByTemplateCode(templateCode);
        if (sameCode != null && !sameCode.getId().equals(request.id())) {
            throw new BusinessException("模板编码已存在: " + templateCode);
        }

        WorkflowTemplateDefinition definition = normalizeDefinition(request);
        int nextVersion = existing == null ? 1 : existing.getVersionNo() + 1;
        ProcessModelBuilder.ProcessModelResult processModelResult =
            processModelBuilder.build(templateCode, nextVersion, request.name(), definition.nodes());

        Deployment deployment = repositoryService.createDeployment()
            .name(request.name())
            .key(templateCode)
            .addBpmnModel(processModelResult.processDefinitionKey() + ".bpmn20.xml", processModelResult.bpmnModel())
            .deploy();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .singleResult();

        LocalDateTime now = LocalDateTime.now();
        WorkflowTemplateEntity entity = existing == null ? new WorkflowTemplateEntity() : existing;
        entity.setTemplateCode(templateCode);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setStatus("DEPLOYED");
        entity.setVersionNo(nextVersion);
        entity.setProcessDefinitionKey(processModelResult.processDefinitionKey());
        entity.setProcessDefinitionId(processDefinition.getId());
        entity.setBpmnXml(processModelResult.bpmnXml());
        entity.setDefinitionJson(payloadService.toJson(definition));
        entity.setUpdatedAt(now);
        if (existing == null) {
            entity.setCreatedAt(now);
            workflowTemplateMapper.insert(entity);
        } else {
            workflowTemplateMapper.update(entity);
        }
        return toView(entity);
    }

    public List<ApiModels.WorkflowTemplateView> list() {
        return workflowTemplateMapper.findAll().stream()
            .map(this::toView)
            .toList();
    }

    public ApiModels.WorkflowTemplateView getById(Long id) {
        WorkflowTemplateEntity entity = workflowTemplateMapper.findById(id);
        if (entity == null) {
            throw new BusinessException("流程模板不存在");
        }
        return toView(entity);
    }

    private ApiModels.WorkflowTemplateView toView(WorkflowTemplateEntity entity) {
        return new ApiModels.WorkflowTemplateView(
            entity.getId(),
            entity.getTemplateCode(),
            entity.getName(),
            entity.getDescription(),
            entity.getStatus(),
            entity.getVersionNo(),
            entity.getProcessDefinitionId(),
            payloadService.parseDefinition(entity.getDefinitionJson()),
            entity.getUpdatedAt()
        );
    }

    private WorkflowTemplateDefinition normalizeDefinition(ApiModels.TemplateSaveRequest request) {
        List<FormFieldDefinition> startFields = normalizeFields(request.startFormFields(), "发起表单");
        if (request.nodes() == null || request.nodes().isEmpty()) {
            throw new BusinessException("至少需要配置一个处理节点");
        }

        List<WorkflowNodeDefinition> nodes = new ArrayList<>();
        Set<String> nodeKeys = new HashSet<>();
        for (int i = 0; i < request.nodes().size(); i++) {
            WorkflowNodeDefinition node = request.nodes().get(i);
            if (node == null || !StringUtils.hasText(node.nodeName())) {
                throw new BusinessException("第" + (i + 1) + "个节点名称不能为空");
            }
            if (!StringUtils.hasText(node.assignee())) {
                throw new BusinessException("节点[" + node.nodeName() + "]必须配置处理人员");
            }
            String nodeKey = StringUtils.hasText(node.nodeKey()) ? normalizeKey(node.nodeKey()) : "node_" + (i + 1);
            if (!nodeKeys.add(nodeKey)) {
                throw new BusinessException("节点编码重复: " + nodeKey);
            }
            List<FormFieldDefinition> nodeFields = normalizeFields(node.formFields(), "节点[" + node.nodeName() + "]表单");
            nodes.add(new WorkflowNodeDefinition(nodeKey, node.nodeName(), node.assignee(), nodeFields));
        }
        return new WorkflowTemplateDefinition(startFields, nodes);
    }

    private List<FormFieldDefinition> normalizeFields(List<FormFieldDefinition> fields, String scopeName) {
        if (fields == null || fields.isEmpty()) {
            return List.of();
        }
        List<FormFieldDefinition> normalized = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < fields.size(); i++) {
            FormFieldDefinition field = fields.get(i);
            if (field == null || !StringUtils.hasText(field.key()) || !StringUtils.hasText(field.label())) {
                throw new BusinessException(scopeName + "存在字段定义不完整的项");
            }
            String key = normalizeKey(field.key());
            if (!keys.add(key)) {
                throw new BusinessException(scopeName + "字段编码重复: " + key);
            }
            String type = StringUtils.hasText(field.type()) ? field.type().trim() : "input";
            normalized.add(new FormFieldDefinition(
                key,
                field.label().trim(),
                type,
                field.required(),
                field.placeholder(),
                field.options() == null ? List.of() : field.options()
            ));
        }
        return normalized;
    }

    private String normalizeKey(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException("编码不能只包含特殊字符");
        }
        return normalized;
    }
}
