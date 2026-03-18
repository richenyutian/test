package com.example.ticketsystem.service;

import com.example.ticketsystem.api.ApiModels;
import com.example.ticketsystem.common.BusinessException;
import com.example.ticketsystem.entity.FlowDefinitionEntity;
import com.example.ticketsystem.entity.FlowTransitionEntity;
import com.example.ticketsystem.entity.FormDefinitionEntity;
import com.example.ticketsystem.entity.NodeDefinitionEntity;
import com.example.ticketsystem.entity.TicketInstanceEntity;
import com.example.ticketsystem.entity.TicketRecordEntity;
import com.example.ticketsystem.entity.TicketTaskEntity;
import com.example.ticketsystem.mapper.TicketInstanceMapper;
import com.example.ticketsystem.mapper.TicketRecordMapper;
import com.example.ticketsystem.mapper.TicketTaskMapper;
import com.example.ticketsystem.model.FlowGraphDefinition;
import com.example.ticketsystem.model.FormFieldDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketRuntimeService {

    private final TicketInstanceMapper ticketInstanceMapper;
    private final TicketTaskMapper ticketTaskMapper;
    private final TicketRecordMapper ticketRecordMapper;
    private final DefinitionService definitionService;
    private final PayloadService payloadService;

    public TicketRuntimeService(TicketInstanceMapper ticketInstanceMapper,
                                TicketTaskMapper ticketTaskMapper,
                                TicketRecordMapper ticketRecordMapper,
                                DefinitionService definitionService,
                                PayloadService payloadService) {
        this.ticketInstanceMapper = ticketInstanceMapper;
        this.ticketTaskMapper = ticketTaskMapper;
        this.ticketRecordMapper = ticketRecordMapper;
        this.definitionService = definitionService;
        this.payloadService = payloadService;
    }

    @Transactional
    public ApiModels.TicketDetailView startTicket(ApiModels.StartTicketRequest request) {
        FlowDefinitionEntity flow = definitionService.requireFlowEntity(request.flowId());
        FlowGraphDefinition graph = payloadService.parseFlowGraph(flow.getGraphJson());
        Map<Long, NodeDefinitionEntity> nodeMap = buildNodeMap(graph.nodeIds());
        NodeDefinitionEntity startNode = nodeMap.get(flow.getStartNodeId());
        FormDefinitionEntity startForm = definitionService.requireFormEntity(startNode.getFormId());

        List<FormFieldDefinition> startFields = payloadService.parseFormSchema(startForm.getSchemaJson()).fields();
        payloadService.validateFormData(startFields, request.formData(), "开始节点表单");
        Map<String, Object> mergedData = payloadService.mergeData(Map.of(), request.formData());

        LocalDateTime now = LocalDateTime.now();
        TicketInstanceEntity ticket = new TicketInstanceEntity();
        ticket.setTicketNo(generateTicketNo());
        ticket.setFlowId(flow.getId());
        ticket.setFlowName(flow.getName());
        ticket.setTitle(request.title());
        ticket.setApplicant(request.applicant());
        ticket.setStatus("IN_PROGRESS");
        ticket.setBusinessDataJson(payloadService.toJson(mergedData));
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        List<FlowTransitionEntity> nextTransitions = definitionService.getTransitions(flow.getId()).stream()
            .filter(transition -> transition.getFromNodeId().equals(startNode.getId()))
            .toList();
        NodeDefinitionEntity nextNode = resolveNextNode(nextTransitions, request.nextNodeId(), nodeMap, "开始节点");
        ticket.setCurrentNodeId(nextNode.getId());
        ticket.setCurrentNodeName(nextNode.getName());
        ticketInstanceMapper.insert(ticket);

        createTask(ticket, nextNode, now);
        createRecord(ticket, startNode, request.applicant(), request.formData(), mergedData, nextNode, null, now);
        return getTicketDetail(ticket.getId());
    }

    @Transactional
    public ApiModels.TicketDetailView completeTask(Long taskId, ApiModels.CompleteTaskRequest request) {
        TicketTaskEntity task = ticketTaskMapper.findById(taskId);
        if (task == null || !"PENDING".equals(task.getStatus())) {
            throw new BusinessException("任务不存在或已处理");
        }
        if (!task.getAssignee().equals(request.operator())) {
            throw new BusinessException("当前任务处理人应为: " + task.getAssignee());
        }

        TicketInstanceEntity ticket = requireTicket(task.getTicketId());
        FlowDefinitionEntity flow = definitionService.requireFlowEntity(task.getFlowId());
        FlowGraphDefinition graph = payloadService.parseFlowGraph(flow.getGraphJson());
        Map<Long, NodeDefinitionEntity> nodeMap = buildNodeMap(graph.nodeIds());
        NodeDefinitionEntity currentNode = nodeMap.get(task.getNodeId());
        FormDefinitionEntity currentForm = definitionService.requireFormEntity(task.getFormId());
        List<FormFieldDefinition> fields = payloadService.parseFormSchema(currentForm.getSchemaJson()).fields();
        payloadService.validateFormData(fields, request.formData(), "当前节点表单");

        Map<String, Object> mergedData = payloadService.mergeData(payloadService.parseMap(ticket.getBusinessDataJson()), request.formData());
        List<FlowTransitionEntity> nextTransitions = definitionService.getTransitions(flow.getId()).stream()
            .filter(transition -> transition.getFromNodeId().equals(currentNode.getId()))
            .toList();

        NodeDefinitionEntity nextNode = null;
        if (!"CLOSE".equals(currentNode.getNodeState())) {
            nextNode = resolveNextNode(nextTransitions, request.nextNodeId(), nodeMap, currentNode.getName());
        }

        LocalDateTime now = LocalDateTime.now();
        task.setStatus("COMPLETED");
        task.setCompletedAt(now);
        ticketTaskMapper.update(task);

        ticket.setBusinessDataJson(payloadService.toJson(mergedData));
        ticket.setUpdatedAt(now);
        if (nextNode == null) {
            ticket.setCurrentNodeId(null);
            ticket.setCurrentNodeName(null);
            ticket.setStatus("COMPLETED");
            ticket.setFinishedAt(now);
        } else {
            ticket.setCurrentNodeId(nextNode.getId());
            ticket.setCurrentNodeName(nextNode.getName());
            ticket.setStatus("IN_PROGRESS");
            createTask(ticket, nextNode, now);
        }
        ticketInstanceMapper.update(ticket);
        createRecord(ticket, currentNode, request.operator(), request.formData(), mergedData, nextNode, request.comment(), now);
        return getTicketDetail(ticket.getId());
    }

    public List<ApiModels.TaskView> listTasks(String assignee) {
        return ticketTaskMapper.findPendingByAssignee(assignee).stream()
            .map(this::toTaskView)
            .toList();
    }

    public List<ApiModels.TicketView> listTickets(String keyword, String status) {
        return ticketInstanceMapper.search(keyword, status).stream()
            .map(ticket -> new ApiModels.TicketView(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getFlowName(),
                ticket.getTitle(),
                ticket.getApplicant(),
                ticket.getCurrentNodeName(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getFinishedAt()
            ))
            .toList();
    }

    public ApiModels.TicketDetailView getTicketDetail(Long ticketId) {
        TicketInstanceEntity ticket = requireTicket(ticketId);
        List<ApiModels.TicketRecordView> records = ticketRecordMapper.findByTicketId(ticketId).stream()
            .map(record -> new ApiModels.TicketRecordView(
                record.getId(),
                record.getNodeName(),
                record.getNodeState(),
                record.getAssignee(),
                record.getOperator(),
                record.getActionType(),
                record.getComment(),
                record.getSelectedNextNodeId(),
                record.getSelectedNextNodeName(),
                payloadService.parseMap(record.getSubmittedDataJson()),
                payloadService.parseMap(record.getMergedDataJson()),
                record.getCreatedAt()
            ))
            .toList();
        return new ApiModels.TicketDetailView(
            ticket.getId(),
            ticket.getTicketNo(),
            ticket.getFlowName(),
            ticket.getTitle(),
            ticket.getApplicant(),
            ticket.getCurrentNodeId(),
            ticket.getCurrentNodeName(),
            ticket.getStatus(),
            payloadService.parseMap(ticket.getBusinessDataJson()),
            records,
            ticket.getCreatedAt(),
            ticket.getUpdatedAt(),
            ticket.getFinishedAt()
        );
    }

    private ApiModels.TaskView toTaskView(TicketTaskEntity task) {
        TicketInstanceEntity ticket = requireTicket(task.getTicketId());
        FlowDefinitionEntity flow = definitionService.requireFlowEntity(task.getFlowId());
        FlowGraphDefinition graph = payloadService.parseFlowGraph(flow.getGraphJson());
        Map<Long, NodeDefinitionEntity> nodeMap = buildNodeMap(graph.nodeIds());
        NodeDefinitionEntity node = nodeMap.get(task.getNodeId());
        FormDefinitionEntity form = definitionService.requireFormEntity(task.getFormId());
        List<FormFieldDefinition> formFields = payloadService.parseFormSchema(form.getSchemaJson()).fields();
        List<ApiModels.NextNodeOptionView> nextOptions = buildNextOptions(flow.getId(), node, nodeMap);

        return new ApiModels.TaskView(
            task.getId(),
            ticket.getId(),
            ticket.getTicketNo(),
            ticket.getTitle(),
            node.getId(),
            node.getName(),
            node.getNodeState(),
            task.getAssignee(),
            task.getActionType(),
            form.getId(),
            form.getName(),
            formFields,
            payloadService.parseMap(ticket.getBusinessDataJson()),
            nextOptions,
            task.getStartedAt()
        );
    }

    private void createTask(TicketInstanceEntity ticket, NodeDefinitionEntity node, LocalDateTime now) {
        TicketTaskEntity task = new TicketTaskEntity();
        task.setTicketId(ticket.getId());
        task.setFlowId(ticket.getFlowId());
        task.setNodeId(node.getId());
        task.setNodeName(node.getName());
        task.setNodeState(node.getNodeState());
        task.setFormId(node.getFormId());
        task.setAssignee(node.getAssignee());
        task.setActionType(node.getActionType());
        task.setStatus("PENDING");
        task.setStartedAt(now);
        ticketTaskMapper.insert(task);
    }

    private void createRecord(TicketInstanceEntity ticket,
                              NodeDefinitionEntity currentNode,
                              String operator,
                              Map<String, Object> submittedData,
                              Map<String, Object> mergedData,
                              NodeDefinitionEntity nextNode,
                              String comment,
                              LocalDateTime now) {
        TicketRecordEntity record = new TicketRecordEntity();
        record.setTicketId(ticket.getId());
        record.setNodeId(currentNode.getId());
        record.setNodeName(currentNode.getName());
        record.setNodeState(currentNode.getNodeState());
        record.setAssignee(currentNode.getAssignee());
        record.setOperator(operator);
        record.setActionType(currentNode.getActionType());
        record.setComment(comment);
        record.setSelectedNextNodeId(nextNode == null ? null : nextNode.getId());
        record.setSelectedNextNodeName(nextNode == null ? null : nextNode.getName());
        record.setSubmittedDataJson(payloadService.toJson(submittedData == null ? Map.of() : submittedData));
        record.setMergedDataJson(payloadService.toJson(mergedData));
        record.setCreatedAt(now);
        ticketRecordMapper.insert(record);
    }

    private Map<Long, NodeDefinitionEntity> buildNodeMap(List<Long> nodeIds) {
        Map<Long, NodeDefinitionEntity> map = new HashMap<>();
        for (NodeDefinitionEntity node : definitionService.getNodesByIds(nodeIds)) {
            map.put(node.getId(), node);
        }
        return map;
    }

    private NodeDefinitionEntity resolveNextNode(List<FlowTransitionEntity> transitions,
                                                 Long requestedNextNodeId,
                                                 Map<Long, NodeDefinitionEntity> nodeMap,
                                                 String scopeName) {
        if (transitions.isEmpty()) {
            throw new BusinessException("节点[" + scopeName + "]未配置 next 节点");
        }
        if (transitions.size() == 1) {
            return nodeMap.get(transitions.get(0).getToNodeId());
        }
        if (requestedNextNodeId == null) {
            throw new BusinessException("节点[" + scopeName + "]存在多个 next 节点，请明确选择流转方向");
        }
        return transitions.stream()
            .filter(item -> item.getToNodeId().equals(requestedNextNodeId))
            .findFirst()
            .map(item -> nodeMap.get(item.getToNodeId()))
            .orElseThrow(() -> new BusinessException("无效的 next 节点选择"));
    }

    private List<ApiModels.NextNodeOptionView> buildNextOptions(Long flowId,
                                                                NodeDefinitionEntity currentNode,
                                                                Map<Long, NodeDefinitionEntity> nodeMap) {
        if ("CLOSE".equals(currentNode.getNodeState())) {
            return List.of();
        }
        List<ApiModels.NextNodeOptionView> options = new ArrayList<>();
        for (FlowTransitionEntity transition : definitionService.getTransitions(flowId)) {
            if (!transition.getFromNodeId().equals(currentNode.getId())) {
                continue;
            }
            NodeDefinitionEntity nextNode = nodeMap.get(transition.getToNodeId());
            options.add(new ApiModels.NextNodeOptionView(nextNode.getId(), nextNode.getName(), nextNode.getNodeState()));
        }
        return options;
    }

    private TicketInstanceEntity requireTicket(Long id) {
        TicketInstanceEntity ticket = ticketInstanceMapper.findById(id);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }
        return ticket;
    }

    private String generateTicketNo() {
        return "TK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + (System.nanoTime() % 100000);
    }
}
