package com.example.ticketworkflow.service;

import com.example.ticketworkflow.model.WorkflowNodeDefinition;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ProcessModelBuilder {

    public ProcessModelResult build(String templateCode, int versionNo, String templateName, List<WorkflowNodeDefinition> nodes) {
        String processKey = templateCode + "_v" + versionNo;

        BpmnModel model = new BpmnModel();
        Process process = new Process();
        process.setId(processKey);
        process.setName(templateName);
        model.addProcess(process);

        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        startEvent.setName("开始");
        process.addFlowElement(startEvent);

        String previousNodeId = startEvent.getId();
        for (int index = 0; index < nodes.size(); index++) {
            WorkflowNodeDefinition node = nodes.get(index);
            UserTask userTask = new UserTask();
            userTask.setId(node.nodeKey());
            userTask.setName(node.nodeName());
            userTask.setAssignee(node.assignee());
            process.addFlowElement(userTask);

            addFlow(process, previousNodeId, userTask.getId(), "flow_" + previousNodeId + "_" + userTask.getId());
            previousNodeId = userTask.getId();
        }

        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        endEvent.setName("结束");
        process.addFlowElement(endEvent);
        addFlow(process, previousNodeId, endEvent.getId(), "flow_" + previousNodeId + "_" + endEvent.getId());

        byte[] xmlBytes = new BpmnXMLConverter().convertToXML(model);
        return new ProcessModelResult(processKey, model, new String(xmlBytes, StandardCharsets.UTF_8));
    }

    private void addFlow(Process process, String sourceRef, String targetRef, String flowId) {
        SequenceFlow sequenceFlow = new SequenceFlow(sourceRef, targetRef);
        sequenceFlow.setId(flowId);
        process.addFlowElement(sequenceFlow);

        FlowElement source = process.getFlowElement(sourceRef);
        FlowElement target = process.getFlowElement(targetRef);
        if (source instanceof org.flowable.bpmn.model.FlowNode sourceNode) {
            sourceNode.getOutgoingFlows().add(sequenceFlow);
        }
        if (target instanceof org.flowable.bpmn.model.FlowNode targetNode) {
            targetNode.getIncomingFlows().add(sequenceFlow);
        }
    }

    public record ProcessModelResult(String processDefinitionKey, BpmnModel bpmnModel, String bpmnXml) {
    }
}
