package com.liujcc.activiti;

import com.google.common.collect.Maps;
import com.liujcc.activiti.conf.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.model.payloads.DeleteProcessPayload;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.runtime.api.impl.ProcessRuntimeImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ActivitiApplicationTests {

    @Autowired
    ProcessEngine processEngine;
    @Autowired
    ProcessRuntime processRuntime;
    @Autowired
    private TaskRuntime taskRuntime;
    @Autowired
    private HistoryService historyService;

    @Autowired
    private SecurityUtil securityUtil;


    @Test
    public void bpmnJSDeployment() {
        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment()
                .addClasspathResource("processes/bpmn/bpmn-js.bpmn")
                //.addClasspathResource("processes/processVariables.bpmn")
                .name("bpmn-js")
                .deploy();
        log.info("部署id=[{}]", deployment.getId());
        log.info("部署名称=[{}]", deployment.getName());
    }

    @Test
    public void contextLoads() {
        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment()
                .addClasspathResource("processes/helloword/helloword.png")
                .addClasspathResource("processes/helloword/helloword.bpmn")
                .name("helloword复杂流程设计")
                .deploy();
        log.info("部署id=[{}]", deployment.getId());
        log.info("部署名称=[{}]", deployment.getName());
    }

    @Test
    public void createProcessInstance() {
        String processDefinitionKey = "helloword";
        HashMap<String, Object> hashMap = Maps.newHashMap();
//        hashMap.put("A", "AAA");
//        hashMap.put("B", "BBB");
         hashMap.put("day", "10");
        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey(processDefinitionKey, hashMap);

        log.info("流程实例ID:[{}]", processInstance.getId());//流程实例ID    101
        log.info("流程定义ID:[{}]", processInstance.getProcessDefinitionId());//流程定义ID   helloworld:1:4

    }
    /**
     * 完成我的任务
     */
    @Test
    public void completeMyPersonalTask() {
        //任务ID
        String taskId = "8f46b884-c8ab-11e9-9016-005056c00008";
        HashMap<String, Object> hashMap = Maps.newHashMap();
         hashMap.put("msg", "驳回");
        //hashMap.put("day", 10);
        processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .complete(taskId, hashMap);
        log.info("完成任务：任务ID：[{}]", taskId);
    }

    @Test
    public void deleteProcessInstance() {
//        processEngine.getRuntimeService().deleteProcessInstance(
//                "e2c6ca11-c5b2-11e9-a13d-507b9d81ab9d",
//                "删除");
        securityUtil.logInAs("system");
         processRuntime.delete(
                 new DeleteProcessPayload(
                         "2a0a7c78-c5ba-11e9-9bfd-507b9d81ab9d",
                         "删除"));

//        CompleteTaskPayload dd = TaskPayloadBuilder.complete().withTaskId("dd").build();
        //taskRuntime.complete(new CompleteTaskPayload());
    }

}
