package com.example.ticketworkflow.service;

import com.example.ticketworkflow.api.ApiModels;
import com.example.ticketworkflow.common.BusinessException;
import com.example.ticketworkflow.entity.TicketFieldIndexEntity;
import com.example.ticketworkflow.entity.TicketInstanceEntity;
import com.example.ticketworkflow.entity.TicketRecordEntity;
import com.example.ticketworkflow.entity.WorkflowTemplateEntity;
import com.example.ticketworkflow.mapper.TicketFieldIndexMapper;
import com.example.ticketworkflow.mapper.TicketInstanceMapper;
import com.example.ticketworkflow.mapper.TicketRecordMapper;
import com.example.ticketworkflow.mapper.WorkflowTemplateMapper;
import com.example.ticketworkflow.model.WorkflowNodeDefinition;
import com.example.ticketworkflow.model.WorkflowTemplateDefinition;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TicketWorkflowService {

    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final TicketInstanceMapper ticketInstanceMapper;
    private final TicketRecordMapper ticketRecordMapper;
    private final TicketFieldIndexMapper ticketFieldIndexMapper;
    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final WorkflowPayloadService payloadService;

    public TicketWorkflowService(WorkflowTemplateMapper workflowTemplateMapper,
                                 TicketInstanceMapper ticketInstanceMapper,
                                 TicketRecordMapper ticketRecordMapper,
                                 TicketFieldIndexMapper ticketFieldIndexMapper,
                                 TaskService taskService,
                                 RuntimeService runtimeService,
                                 WorkflowPayloadService payloadService) {
        this.workflowTemplateMapper = workflowTemplateMapper;
        this.ticketInstanceMapper = ticketInstanceMapper;
        this.ticketRecordMapper = ticketRecordMapper;
        this.ticketFieldIndexMapper = ticketFieldIndexMapper;
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.payloadService = payloadService;
    }

    @Transactional
    public ApiModels.TicketInstanceDetailView start(ApiModels.StartProcessRequest request) {
        WorkflowTemplateEntity template = requireTemplate(request.templateId());
        WorkflowTemplateDefinition definition = payloadService.parseDefinition(template.getDefinitionJson());
        payloadService.validateRequiredFields(definition.startFormFields(), request.formData(), "发起表单");

        Map<String, Object> mergedFormData = payloadService.mergeFormData(Map.of(), request.formData());
        String businessKey = generateBusinessKey();

        Map<String, Object> variables = new HashMap<>();
        variables.put("businessKey", businessKey);
        variables.put("initiator", request.initiator());
        variables.put("ticketTitle", request.title());
        variables.put("mergedFormData", payloadService.toJson(mergedFormData));

        ProcessInstance processInstance =
            runtimeService.startProcessInstanceById(template.getProcessDefinitionId(), businessKey, variables);
        Task currentTask = taskService.createTaskQuery()
            .processInstanceId(processInstance.getProcessInstanceId())
            .active()
            .singleResult();

        LocalDateTime now = LocalDateTime.now();
        TicketInstanceEntity entity = new TicketInstanceEntity();
        entity.setBusinessKey(businessKey);
        entity.setTemplateId(template.getId());
        entity.setTemplateCode(template.getTemplateCode());
        entity.setTemplateName(template.getName());
        entity.setProcessDefinitionId(template.getProcessDefinitionId());
        entity.setProcessInstanceId(processInstance.getProcessInstanceId());
        entity.setTitle(request.title());
        entity.setInitiator(request.initiator());
        entity.setCurrentNodeKey(currentTask == null ? null : currentTask.getTaskDefinitionKey());
        entity.setCurrentNodeName(currentTask == null ? null : currentTask.getName());
        entity.setStatus(currentTask == null ? "COMPLETED" : "RUNNING");
        entity.setMergedFormData(payloadService.toJson(mergedFormData));
        entity.setSearchableText(payloadService.buildSearchableText(request.title(), mergedFormData));
        entity.setStartedAt(now);
        entity.setEndedAt(currentTask == null ? now : null);
        entity.setUpdatedAt(now);
        ticketInstanceMapper.insert(entity);

        TicketRecordEntity record = new TicketRecordEntity();
        record.setInstanceId(entity.getId());
        record.setProcessInstanceId(entity.getProcessInstanceId());
        record.setTaskId(null);
        record.setTaskDefinitionKey("start");
        record.setTaskName("发起流程");
        record.setAssignee(request.initiator());
        record.setOperator(request.initiator());
        record.setAction("START");
        record.setComment("流程已发起");
        record.setSubmittedFormData(payloadService.toJson(mergedFormData));
        record.setMergedFormData(payloadService.toJson(mergedFormData));
        record.setCreatedAt(now);
        ticketRecordMapper.insert(record);

        refreshFieldIndex(entity.getId(), definition, mergedFormData, now);
        return getInstanceDetail(entity.getId());
    }

    @Transactional
    public ApiModels.TicketInstanceDetailView completeTask(String taskId, ApiModels.CompleteTaskRequest request) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已处理");
        }

        TicketInstanceEntity instance = requireInstanceByProcessInstanceId(task.getProcessInstanceId());
        WorkflowTemplateEntity template = requireTemplate(instance.getTemplateId());
        WorkflowTemplateDefinition definition = payloadService.parseDefinition(template.getDefinitionJson());
        WorkflowNodeDefinition nodeDefinition = requireNode(definition, task.getTaskDefinitionKey());

        if (StringUtils.hasText(task.getAssignee()) && !Objects.equals(task.getAssignee(), request.operator())) {
            throw new BusinessException("当前任务处理人应为: " + task.getAssignee());
        }

        payloadService.validateRequiredFields(nodeDefinition.formFields(), request.formData(), "节点[" + nodeDefinition.nodeName() + "]表单");
        Map<String, Object> currentData = payloadService.parseMap(instance.getMergedFormData());
        Map<String, Object> mergedFormData = payloadService.mergeFormData(currentData, request.formData());

        Map<String, Object> variables = new HashMap<>();
        variables.put("lastOperator", request.operator());
        variables.put("mergedFormData", payloadService.toJson(mergedFormData));
        taskService.complete(taskId, variables);

        Task nextTask = taskService.createTaskQuery()
            .processInstanceId(task.getProcessInstanceId())
            .active()
            .singleResult();
        LocalDateTime now = LocalDateTime.now();
        instance.setCurrentNodeKey(nextTask == null ? null : nextTask.getTaskDefinitionKey());
        instance.setCurrentNodeName(nextTask == null ? null : nextTask.getName());
        instance.setStatus(nextTask == null ? "COMPLETED" : "RUNNING");
        instance.setMergedFormData(payloadService.toJson(mergedFormData));
        instance.setSearchableText(payloadService.buildSearchableText(instance.getTitle(), mergedFormData));
        instance.setEndedAt(nextTask == null ? now : null);
        instance.setUpdatedAt(now);
        ticketInstanceMapper.update(instance);

        TicketRecordEntity record = new TicketRecordEntity();
        record.setInstanceId(instance.getId());
        record.setProcessInstanceId(instance.getProcessInstanceId());
        record.setTaskId(task.getId());
        record.setTaskDefinitionKey(task.getTaskDefinitionKey());
        record.setTaskName(task.getName());
        record.setAssignee(task.getAssignee());
        record.setOperator(request.operator());
        record.setAction("COMPLETE");
        record.setComment(request.comment());
        record.setSubmittedFormData(payloadService.toJson(request.formData() == null ? Map.of() : request.formData()));
        record.setMergedFormData(payloadService.toJson(mergedFormData));
        record.setCreatedAt(now);
        ticketRecordMapper.insert(record);

        refreshFieldIndex(instance.getId(), definition, mergedFormData, now);
        return getInstanceDetail(instance.getId());
    }

    public List<ApiModels.TaskView> listTasks(String assignee) {
        return taskService.createTaskQuery()
            .taskAssignee(assignee)
            .active()
            .orderByTaskCreateTime()
            .desc()
            .list()
            .stream()
            .map(this::toTaskView)
            .toList();
    }

    public List<ApiModels.TicketInstanceView> searchInstances(String keyword,
                                                              String initiator,
                                                              String status,
                                                              String fieldKey,
                                                              String fieldValue) {
        List<Long> instanceIds = null;
        if (StringUtils.hasText(fieldKey) || StringUtils.hasText(fieldValue)) {
            if (!StringUtils.hasText(fieldKey) || !StringUtils.hasText(fieldValue)) {
                throw new BusinessException("字段检索需要同时提供fieldKey和fieldValue");
            }
            instanceIds = ticketFieldIndexMapper.findInstanceIdsByField(fieldKey, fieldValue);
            if (instanceIds.isEmpty()) {
                return List.of();
            }
        }

        return ticketInstanceMapper.search(keyword, initiator, status, instanceIds)
            .stream()
            .map(instance -> new ApiModels.TicketInstanceView(
                instance.getId(),
                instance.getBusinessKey(),
                instance.getTemplateName(),
                instance.getTitle(),
                instance.getInitiator(),
                instance.getCurrentNodeName(),
                instance.getStatus(),
                instance.getStartedAt(),
                instance.getEndedAt(),
                instance.getUpdatedAt()
            ))
            .toList();
    }

    public ApiModels.TicketInstanceDetailView getInstanceDetail(Long instanceId) {
        TicketInstanceEntity instance = ticketInstanceMapper.findById(instanceId);
        if (instance == null) {
            throw new BusinessException("工单实例不存在");
        }

        List<ApiModels.TicketRecordView> records = ticketRecordMapper.findByInstanceId(instanceId)
            .stream()
            .map(record -> new ApiModels.TicketRecordView(
                record.getId(),
                record.getTaskDefinitionKey(),
                record.getTaskName(),
                record.getAssignee(),
                record.getOperator(),
                record.getAction(),
                record.getComment(),
                payloadService.parseMap(record.getSubmittedFormData()),
                payloadService.parseMap(record.getMergedFormData()),
                record.getCreatedAt()
            ))
            .toList();

        List<ApiModels.TicketFieldView> fields = ticketFieldIndexMapper.findByInstanceId(instanceId)
            .stream()
            .map(field -> new ApiModels.TicketFieldView(field.getFieldKey(), field.getFieldLabel(), field.getFieldValue()))
            .toList();

        return new ApiModels.TicketInstanceDetailView(
            instance.getId(),
            instance.getBusinessKey(),
            instance.getTemplateId(),
            instance.getTemplateName(),
            instance.getTitle(),
            instance.getInitiator(),
            instance.getCurrentNodeName(),
            instance.getStatus(),
            payloadService.parseMap(instance.getMergedFormData()),
            fields,
            records,
            instance.getStartedAt(),
            instance.getEndedAt(),
            instance.getUpdatedAt()
        );
    }

    private ApiModels.TaskView toTaskView(Task task) {
        TicketInstanceEntity instance = requireInstanceByProcessInstanceId(task.getProcessInstanceId());
        WorkflowTemplateEntity template = requireTemplate(instance.getTemplateId());
        WorkflowTemplateDefinition definition = payloadService.parseDefinition(template.getDefinitionJson());
        WorkflowNodeDefinition nodeDefinition = requireNode(definition, task.getTaskDefinitionKey());

        return new ApiModels.TaskView(
            task.getId(),
            task.getName(),
            task.getTaskDefinitionKey(),
            task.getAssignee(),
            instance.getId(),
            instance.getBusinessKey(),
            instance.getTitle(),
            instance.getInitiator(),
            nodeDefinition.formFields(),
            payloadService.parseMap(instance.getMergedFormData()),
            task.getCreateTime() == null ? null : LocalDateTime.ofInstant(task.getCreateTime().toInstant(), java.time.ZoneId.systemDefault())
        );
    }

    private void refreshFieldIndex(Long instanceId,
                                   WorkflowTemplateDefinition definition,
                                   Map<String, Object> mergedFormData,
                                   LocalDateTime now) {
        Map<String, String> labelMap = payloadService.buildFieldLabelMap(definition);
        ticketFieldIndexMapper.deleteByInstanceId(instanceId);
        for (Map.Entry<String, Object> entry : mergedFormData.entrySet()) {
            TicketFieldIndexEntity fieldIndex = new TicketFieldIndexEntity();
            fieldIndex.setInstanceId(instanceId);
            fieldIndex.setFieldKey(entry.getKey());
            fieldIndex.setFieldLabel(labelMap.getOrDefault(entry.getKey(), entry.getKey()));
            fieldIndex.setFieldValue(entry.getValue() == null ? null : String.valueOf(entry.getValue()));
            fieldIndex.setUpdatedAt(now);
            ticketFieldIndexMapper.insert(fieldIndex);
        }
    }

    private WorkflowTemplateEntity requireTemplate(Long templateId) {
        WorkflowTemplateEntity template = workflowTemplateMapper.findById(templateId);
        if (template == null) {
            throw new BusinessException("流程模板不存在");
        }
        return template;
    }

    private TicketInstanceEntity requireInstanceByProcessInstanceId(String processInstanceId) {
        TicketInstanceEntity instance = ticketInstanceMapper.findByProcessInstanceId(processInstanceId);
        if (instance == null) {
            throw new BusinessException("工单实例不存在");
        }
        return instance;
    }

    private WorkflowNodeDefinition requireNode(WorkflowTemplateDefinition definition, String nodeKey) {
        return definition.nodes().stream()
            .filter(node -> Objects.equals(node.nodeKey(), nodeKey))
            .findFirst()
            .orElseThrow(() -> new BusinessException("节点定义不存在: " + nodeKey));
    }

    private String generateBusinessKey() {
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "TICKET-" + prefix + "-" + System.nanoTime() % 100000;
    }
}
