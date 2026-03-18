package com.example.ticketsystem.service;

import com.example.ticketsystem.api.ApiModels;
import com.example.ticketsystem.common.BusinessException;
import com.example.ticketsystem.entity.FlowDefinitionEntity;
import com.example.ticketsystem.entity.FlowTransitionEntity;
import com.example.ticketsystem.entity.FormDefinitionEntity;
import com.example.ticketsystem.entity.NodeDefinitionEntity;
import com.example.ticketsystem.mapper.FlowDefinitionMapper;
import com.example.ticketsystem.mapper.FlowTransitionMapper;
import com.example.ticketsystem.mapper.FormDefinitionMapper;
import com.example.ticketsystem.mapper.NodeDefinitionMapper;
import com.example.ticketsystem.model.FlowGraphDefinition;
import com.example.ticketsystem.model.FlowTransitionDefinition;
import com.example.ticketsystem.model.FormFieldDefinition;
import com.example.ticketsystem.model.FormSchemaDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class DefinitionService {

    private static final Set<String> NODE_STATES = Set.of("START", "AUDIT", "HANDLE", "CLOSE");
    private static final Set<String> ACTION_TYPES = Set.of("SUBMIT", "APPROVE", "PROCESS", "FINISH");

    private final FormDefinitionMapper formDefinitionMapper;
    private final NodeDefinitionMapper nodeDefinitionMapper;
    private final FlowDefinitionMapper flowDefinitionMapper;
    private final FlowTransitionMapper flowTransitionMapper;
    private final PayloadService payloadService;

    public DefinitionService(FormDefinitionMapper formDefinitionMapper,
                             NodeDefinitionMapper nodeDefinitionMapper,
                             FlowDefinitionMapper flowDefinitionMapper,
                             FlowTransitionMapper flowTransitionMapper,
                             PayloadService payloadService) {
        this.formDefinitionMapper = formDefinitionMapper;
        this.nodeDefinitionMapper = nodeDefinitionMapper;
        this.flowDefinitionMapper = flowDefinitionMapper;
        this.flowTransitionMapper = flowTransitionMapper;
        this.payloadService = payloadService;
    }

    @Transactional
    public ApiModels.FormView saveForm(ApiModels.FormSaveRequest request) {
        validateFormFields(request.fields());
        FormDefinitionEntity existing = request.id() == null ? null : requireFormEntity(request.id());
        String formCode = StringUtils.hasText(request.formCode()) ? normalizeCode(request.formCode()) : normalizeCode(request.name());
        FormDefinitionEntity sameCode = formDefinitionMapper.findByFormCode(formCode);
        if (sameCode != null && !sameCode.getId().equals(request.id())) {
            throw new BusinessException("表单编码已存在: " + formCode);
        }

        LocalDateTime now = LocalDateTime.now();
        FormDefinitionEntity entity = existing == null ? new FormDefinitionEntity() : existing;
        entity.setFormCode(formCode);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setStatus("ACTIVE");
        entity.setSchemaJson(payloadService.toJson(new FormSchemaDefinition(request.fields())));
        entity.setUpdatedAt(now);
        if (existing == null) {
            entity.setCreatedAt(now);
            formDefinitionMapper.insert(entity);
        } else {
            formDefinitionMapper.update(entity);
        }
        return toFormView(entity);
    }

    public List<ApiModels.FormView> listForms() {
        return formDefinitionMapper.findAll().stream().map(this::toFormView).toList();
    }

    @Transactional
    public ApiModels.NodeView saveNode(ApiModels.NodeSaveRequest request) {
        String nodeState = normalizeEnum(request.nodeState());
        String actionType = normalizeEnum(request.actionType());
        if (!NODE_STATES.contains(nodeState)) {
            throw new BusinessException("不支持的节点状态: " + request.nodeState());
        }
        if (!ACTION_TYPES.contains(actionType)) {
            throw new BusinessException("不支持的节点执行动作: " + request.actionType());
        }
        FormDefinitionEntity form = requireFormEntity(request.formId());
        NodeDefinitionEntity existing = request.id() == null ? null : requireNodeEntity(request.id());
        String nodeCode = StringUtils.hasText(request.nodeCode()) ? normalizeCode(request.nodeCode()) : normalizeCode(request.name());
        NodeDefinitionEntity sameCode = nodeDefinitionMapper.findByNodeCode(nodeCode);
        if (sameCode != null && !sameCode.getId().equals(request.id())) {
            throw new BusinessException("节点编码已存在: " + nodeCode);
        }

        LocalDateTime now = LocalDateTime.now();
        NodeDefinitionEntity entity = existing == null ? new NodeDefinitionEntity() : existing;
        entity.setNodeCode(nodeCode);
        entity.setName(request.name());
        entity.setNodeState(nodeState);
        entity.setFormId(form.getId());
        entity.setAssignee(request.assignee());
        entity.setActionType(actionType);
        entity.setDescription(request.description());
        entity.setUpdatedAt(now);
        if (existing == null) {
            entity.setCreatedAt(now);
            nodeDefinitionMapper.insert(entity);
        } else {
            nodeDefinitionMapper.update(entity);
        }
        return toNodeView(entity, form);
    }

    public List<ApiModels.NodeView> listNodes() {
        List<FormDefinitionEntity> forms = formDefinitionMapper.findAll();
        Map<Long, FormDefinitionEntity> formMap = new HashMap<>();
        for (FormDefinitionEntity form : forms) {
            formMap.put(form.getId(), form);
        }
        return nodeDefinitionMapper.findAll().stream()
            .map(node -> toNodeView(node, formMap.get(node.getFormId())))
            .toList();
    }

    @Transactional
    public ApiModels.FlowView saveFlow(ApiModels.FlowSaveRequest request) {
        FlowDefinitionEntity existing = request.id() == null ? null : requireFlowEntity(request.id());
        String flowCode = StringUtils.hasText(request.flowCode()) ? normalizeCode(request.flowCode()) : normalizeCode(request.name());
        FlowDefinitionEntity sameCode = flowDefinitionMapper.findByFlowCode(flowCode);
        if (sameCode != null && !sameCode.getId().equals(request.id())) {
            throw new BusinessException("流程编码已存在: " + flowCode);
        }

        List<Long> uniqueNodeIds = new ArrayList<>(new LinkedHashSet<>(request.nodeIds()));
        Map<Long, NodeDefinitionEntity> nodeMap = loadNodeMap(uniqueNodeIds);
        validateFlow(request, nodeMap);

        FlowGraphDefinition graphDefinition = new FlowGraphDefinition(uniqueNodeIds, request.transitions());
        LocalDateTime now = LocalDateTime.now();
        FlowDefinitionEntity entity = existing == null ? new FlowDefinitionEntity() : existing;
        entity.setFlowCode(flowCode);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setStatus("ACTIVE");
        entity.setStartNodeId(request.startNodeId());
        entity.setGraphJson(payloadService.toJson(graphDefinition));
        entity.setUpdatedAt(now);
        if (existing == null) {
            entity.setCreatedAt(now);
            flowDefinitionMapper.insert(entity);
        } else {
            flowDefinitionMapper.update(entity);
            flowTransitionMapper.deleteByFlowId(entity.getId());
        }

        for (FlowTransitionDefinition transition : request.transitions()) {
            FlowTransitionEntity transitionEntity = new FlowTransitionEntity();
            transitionEntity.setFlowId(entity.getId());
            transitionEntity.setFromNodeId(transition.fromNodeId());
            transitionEntity.setToNodeId(transition.toNodeId());
            transitionEntity.setTransitionName(transition.transitionName());
            transitionEntity.setSortNo(transition.sortNo() == null ? 0 : transition.sortNo());
            flowTransitionMapper.insert(transitionEntity);
        }

        return buildFlowView(entity);
    }

    public List<ApiModels.FlowView> listFlows() {
        return flowDefinitionMapper.findAll().stream()
            .map(this::buildFlowView)
            .toList();
    }

    public ApiModels.FlowView getFlow(Long id) {
        return buildFlowView(requireFlowEntity(id));
    }

    public FormDefinitionEntity requireFormEntity(Long id) {
        FormDefinitionEntity entity = formDefinitionMapper.findById(id);
        if (entity == null) {
            throw new BusinessException("表单不存在");
        }
        return entity;
    }

    public NodeDefinitionEntity requireNodeEntity(Long id) {
        NodeDefinitionEntity entity = nodeDefinitionMapper.findById(id);
        if (entity == null) {
            throw new BusinessException("节点不存在");
        }
        return entity;
    }

    public FlowDefinitionEntity requireFlowEntity(Long id) {
        FlowDefinitionEntity entity = flowDefinitionMapper.findById(id);
        if (entity == null) {
            throw new BusinessException("流程不存在");
        }
        return entity;
    }

    public List<FlowTransitionEntity> getTransitions(Long flowId) {
        return flowTransitionMapper.findByFlowId(flowId);
    }

    public List<NodeDefinitionEntity> getNodesByIds(List<Long> nodeIds) {
        return nodeDefinitionMapper.findByIds(nodeIds);
    }

    private ApiModels.FormView toFormView(FormDefinitionEntity entity) {
        return new ApiModels.FormView(
            entity.getId(),
            entity.getFormCode(),
            entity.getName(),
            entity.getDescription(),
            entity.getStatus(),
            payloadService.parseFormSchema(entity.getSchemaJson()).fields(),
            entity.getUpdatedAt()
        );
    }

    private ApiModels.NodeView toNodeView(NodeDefinitionEntity node, FormDefinitionEntity form) {
        if (form == null) {
            throw new BusinessException("节点关联的表单不存在: " + node.getFormId());
        }
        return new ApiModels.NodeView(
            node.getId(),
            node.getNodeCode(),
            node.getName(),
            node.getNodeState(),
            node.getFormId(),
            form.getName(),
            node.getAssignee(),
            node.getActionType(),
            node.getDescription(),
            payloadService.parseFormSchema(form.getSchemaJson()).fields(),
            node.getUpdatedAt()
        );
    }

    private ApiModels.FlowView buildFlowView(FlowDefinitionEntity flow) {
        FlowGraphDefinition graph = payloadService.parseFlowGraph(flow.getGraphJson());
        List<NodeDefinitionEntity> nodes = getNodesByIds(graph.nodeIds());
        Map<Long, NodeDefinitionEntity> nodeMap = new HashMap<>();
        for (NodeDefinitionEntity node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        Map<Long, FormDefinitionEntity> formMap = new HashMap<>();
        for (FormDefinitionEntity form : formDefinitionMapper.findAll()) {
            formMap.put(form.getId(), form);
        }

        List<ApiModels.FlowNodeView> flowNodes = graph.nodeIds().stream()
            .map(nodeId -> {
                NodeDefinitionEntity node = nodeMap.get(nodeId);
                if (node == null) {
                    throw new BusinessException("流程引用了不存在的节点: " + nodeId);
                }
                FormDefinitionEntity form = formMap.get(node.getFormId());
                return new ApiModels.FlowNodeView(
                    node.getId(),
                    node.getName(),
                    node.getNodeState(),
                    node.getFormId(),
                    form == null ? null : form.getName(),
                    node.getAssignee(),
                    node.getActionType(),
                    form == null ? List.of() : payloadService.parseFormSchema(form.getSchemaJson()).fields()
                );
            })
            .toList();

        List<ApiModels.FlowTransitionView> transitions = flowTransitionMapper.findByFlowId(flow.getId()).stream()
            .map(item -> new ApiModels.FlowTransitionView(
                item.getFromNodeId(),
                nodeMap.get(item.getFromNodeId()) == null ? null : nodeMap.get(item.getFromNodeId()).getName(),
                item.getToNodeId(),
                nodeMap.get(item.getToNodeId()) == null ? null : nodeMap.get(item.getToNodeId()).getName(),
                item.getTransitionName(),
                item.getSortNo()
            ))
            .toList();

        return new ApiModels.FlowView(
            flow.getId(),
            flow.getFlowCode(),
            flow.getName(),
            flow.getDescription(),
            flow.getStatus(),
            flow.getStartNodeId(),
            flowNodes,
            transitions,
            flow.getUpdatedAt()
        );
    }

    private void validateFormFields(List<FormFieldDefinition> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new BusinessException("表单至少需要一个字段");
        }
        Set<String> keys = new HashSet<>();
        for (FormFieldDefinition field : fields) {
            if (field == null || !StringUtils.hasText(field.key()) || !StringUtils.hasText(field.label())) {
                throw new BusinessException("表单字段定义不完整");
            }
            String key = normalizeCode(field.key());
            if (!keys.add(key)) {
                throw new BusinessException("表单字段编码重复: " + key);
            }
        }
    }

    private void validateFlow(ApiModels.FlowSaveRequest request, Map<Long, NodeDefinitionEntity> nodeMap) {
        if (!nodeMap.containsKey(request.startNodeId())) {
            throw new BusinessException("开始节点不存在于流程节点列表中");
        }
        NodeDefinitionEntity startNode = nodeMap.get(request.startNodeId());
        if (!"START".equals(startNode.getNodeState())) {
            throw new BusinessException("开始节点必须是 START 类型");
        }

        long startCount = nodeMap.values().stream().filter(node -> "START".equals(node.getNodeState())).count();
        if (startCount != 1) {
            throw new BusinessException("一个流程中必须且只能有一个 START 节点");
        }
        boolean hasClose = nodeMap.values().stream().anyMatch(node -> "CLOSE".equals(node.getNodeState()));
        if (!hasClose) {
            throw new BusinessException("流程中至少需要一个 CLOSE 节点");
        }

        Map<Long, Integer> outgoingCount = new HashMap<>();
        for (FlowTransitionDefinition transition : request.transitions()) {
            if (!nodeMap.containsKey(transition.fromNodeId()) || !nodeMap.containsKey(transition.toNodeId())) {
                throw new BusinessException("流转关系中存在未加入流程的节点");
            }
            NodeDefinitionEntity fromNode = nodeMap.get(transition.fromNodeId());
            if ("CLOSE".equals(fromNode.getNodeState())) {
                throw new BusinessException("CLOSE 节点不能再配置 next 节点");
            }
            outgoingCount.merge(transition.fromNodeId(), 1, Integer::sum);
        }

        for (NodeDefinitionEntity node : nodeMap.values()) {
            if ("CLOSE".equals(node.getNodeState())) {
                continue;
            }
            if (outgoingCount.getOrDefault(node.getId(), 0) < 1) {
                throw new BusinessException("节点[" + node.getName() + "]至少需要一个 next 节点");
            }
        }
    }

    private Map<Long, NodeDefinitionEntity> loadNodeMap(List<Long> nodeIds) {
        List<NodeDefinitionEntity> nodes = nodeDefinitionMapper.findByIds(nodeIds);
        if (nodes.size() != new HashSet<>(nodeIds).size()) {
            throw new BusinessException("存在未找到的节点定义");
        }
        Map<Long, NodeDefinitionEntity> map = new HashMap<>();
        for (NodeDefinitionEntity node : nodes) {
            map.put(node.getId(), node);
        }
        return map;
    }

    private String normalizeCode(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException("编码不能为空");
        }
        return normalized;
    }

    private String normalizeEnum(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
