package com.liujcc.activiti.controller;

import com.google.common.collect.Maps;
import com.liujcc.activiti.conf.SecurityUtil;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.ProcessInstanceMeta;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReST controller to interact with deployed process definitions
 */
@RestController
public class ProcessDefinitionsController {
    private Logger logger = LoggerFactory.getLogger(ProcessDefinitionsController.class);

    @Autowired
    private ProcessRuntime processRuntime;
    @Autowired
    private TaskRuntime taskRuntime;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SecurityUtil securityUtil;

    @ModelAttribute
    public void init() {
        securityUtil.logInAs("system");
    }

    @GetMapping("/process-definitions")
    public List<ProcessDefinition> getProcessDefinitions() {
        //获取系统流程定义
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 10));
        logger.info("> Available Process definitions: " + processDefinitionPage.getTotalItems());

        for (ProcessDefinition pd : processDefinitionPage.getContent()) {
            logger.info("\t > Process definition: " + pd);
        }

        return processDefinitionPage.getContent();
    }

    @GetMapping("/process-instance-meta/{processInstanceId}")
    public Map<String ,Object> getProcessInstanceMeta(@PathVariable(value = "processInstanceId") String processInstanceId) {
        ProcessInstanceMeta processInstanceMeta = processRuntime.processInstanceMeta(processInstanceId);

        ProcessInstance processInstance = processRuntime.processInstance(processInstanceId);
        //获取当前任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        //获取流程定义
        ProcessDefinition processDefinition = processRuntime.processDefinition(task.getProcessDefinitionId());
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

        //可以获取当前任务坐标信息
        GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(task.getTaskDefinitionKey());
        Process process = bpmnModel.getProcesses().get(0);

        UserTask flowElement = (UserTask)process.getFlowElement(task.getTaskDefinitionKey());
        //获取当前任务图连线 outgoing ,incoming
        List<SequenceFlow> outgoingFlows = flowElement.getOutgoingFlows();
        outgoingFlows.stream().forEach(flows->{
            System.out.println(flows.getName());
            System.out.println(flows.getId());
        });

        HashMap<String, Object> objectObjectHashMap = Maps.newHashMap();
        objectObjectHashMap.put("graphicInfo",graphicInfo);
        objectObjectHashMap.put("flowElement",flowElement);

        return objectObjectHashMap;
    }
}