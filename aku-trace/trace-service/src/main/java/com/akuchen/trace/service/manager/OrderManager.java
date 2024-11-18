package com.akuchen.trace.service.manager;

import com.akuchen.trace.api.common.constant.SystemConstant;
import com.akuchen.trace.api.common.dto.MessageLogDTO;
import com.akuchen.trace.api.common.dto.TapdBugDTO;
import com.akuchen.trace.api.common.dto.TraceOrderDTO;
import com.akuchen.trace.api.common.enums.OrderStatusEnum;
import com.akuchen.trace.api.common.enums.TapdBugStatusEnum;
import com.akuchen.trace.api.common.utils.BeanUtils;
import com.akuchen.trace.api.common.utils.MessageLogUtils;
import com.akuchen.trace.api.common.utils.MockFileUtils;
import com.akuchen.trace.service.common.TraceConfig;
import com.akuchen.trace.service.entity.dto.req.AddOrderReqDTO;
import com.akuchen.trace.service.entity.dto.req.RunOrderReqDto;
import com.akuchen.trace.service.service.OrderService;
import com.akuchen.trace.service.service.TapdBugService;
import com.akuchen.trace.service.util.*;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class OrderManager {


    private static String MVN_COMMAND = " /home/akuchen/apache-maven-3.8.8/bin/mvn";

    @Autowired
    private OrderService orderService;
    @Autowired
    private TapdBugService tapdBugService;
    @Autowired
    private GitHandler gitHandler;
    @Autowired
    private TraceConfig traceConfig;
    @Autowired
    private TapdHttpClient tapdHttpClient;

    public void tapdBugCreateOrder(List<TapdBugDTO> tapdBugDTOS) {
        tapdBugDTOS.stream().forEach(t -> {
            try {
                AddOrderReqDTO addOrderReqDTO = new AddOrderReqDTO();
                addOrderReqDTO.setTid(t.getTid());
                addOrderReqDTO.setAppName(t.getAppName());
                addOrderReqDTO.setTapdBugId(t.getTapdBugId());
                addOrderReqDTO.setBugId(t.getId());
                addOrderReqDTO.setDay(0);//只支持当天的异常
                createOrder(addOrderReqDTO);
                tapdBugService.updateStatus(t.getId(), TapdBugStatusEnum.CREATE_ORDER.getStatus());
            } catch (Exception e) {
                log.error("tapd创建任务失败的job失败", e);
            }
        });

    }

    public Boolean createOrder(AddOrderReqDTO addOrderReqDTO) {
        TraceConfig.application application = traceConfig.getApplications().get(addOrderReqDTO.getAppName());
        Optional.ofNullable(application).orElseThrow(() -> new RuntimeException("未配置apollo信息,无法获取项目地址"));//需要重试

        //检查任务是否已经存在, 如果存在, 则不再创建
        // 如果存在并且执行成功 或者失败 直接通触发 bug回复
        // 如果存在并且执行中, 不处理
        TraceOrderDTO traceOrderDTO = orderService.queryByTidAndAppName(addOrderReqDTO.getTid(),addOrderReqDTO.getAppName());
        if(Objects.nonNull(traceOrderDTO) && Objects.nonNull(addOrderReqDTO.getTapdBugId())){

            TapdBugDTO tapdBugDTO = new TapdBugDTO();
            tapdBugDTO.setTapdBugId(addOrderReqDTO.getTapdBugId());
            tapdBugDTO.setId(addOrderReqDTO.getBugId());
            String answer=Objects.equals(traceOrderDTO.getStatus(), OrderStatusEnum.REPLIED.getStatus()) ?
                          traceOrderDTO.getGptAnswer() :
                          OrderStatusEnum.of(traceOrderDTO.getStatus());
            Integer status = Objects.equals(traceOrderDTO.getStatus(), OrderStatusEnum.REPLIED.getStatus()) ?
                             TapdBugStatusEnum.COMPLETE.getStatus() :
                             TapdBugStatusEnum.FAILED.getStatus();
            tapdBugService.commitBug(tapdBugDTO, answer,status);
            return true;
        }

        //准备开始, 检查任务是否在运行中
        Boolean flag = HashMapUtil.check(application.getGitlabUrl());
        if(!flag){
            throw new RuntimeException("项目运行中");
        }
        //
        ThreadPoolUtils.submitTask(()->{
            TraceOrderDTO traceOrderDto = null;
            try {
                traceOrderDto = ConvertUtils.toOrderDto(addOrderReqDTO,application.getGitlabUrl(),traceConfig.getServicePath()+"/"+addOrderReqDTO.getAppName(),application.getServiceName());
                Long id = orderService.insert(traceOrderDto);
                traceOrderDto.setId(id);
                runOrder(BeanUtils.convert(traceOrderDto, RunOrderReqDto.class));
            } catch (java.util.concurrent.TimeoutException e) {
                log.error("tid={}获取超时", addOrderReqDTO.getTid());
            } catch (Exception e) {
                orderService.modifyStatus(traceOrderDto.getId(), OrderStatusEnum.FAILED.getStatus());
                traceOrderDto.setStatus(OrderStatusEnum.FAILED.getStatus());
                log.error("tid={} 发生未知错误", addOrderReqDTO.getTid(), e);
            }finally {
                HashMapUtil.del(application.getGitlabUrl());
            }
        });

        return true;
    }


    public void runOrder(RunOrderReqDto runOrderReqDto) throws Exception {
        /**
         * 1在指定目录创建项目 把代码拉下来
         *
         * 2在分支的test目录 创建mock方法
         *
         * 3用命令的方式 运行mock方法 生成可执行的mockTest
         *
         * 4用命令的方式 运行mocktest 运行成功后 读取GPT回复
         *
         * 5运行状态和回复记录 写db
         */
        //1
        orderService.modifyStatus(runOrderReqDto.getId(),OrderStatusEnum.PROJECT_GENERATION.getStatus());
        Git git = gitHandler.syncRepository(runOrderReqDto.getGitUrl(), runOrderReqDto.getCodePath(), runOrderReqDto.getBranch());
        //先在主目录执行 mvn install   不然后面的mvn 可能会抛异常   mvn clean install -Dmaven.test.skip=true
        CmdExecutor.executeCmd( new String[] { "cd "+runOrderReqDto.getCodePath(), MVN_COMMAND+" clean dependency:resolve -U install -Dmaven.test.skip=true"}, traceConfig.getCommandTimeout());
        if (Objects.isNull(git)) {
            throw new RuntimeException("Failed to sync repository");
        }
        //1去指定的项目(有子项目选子项目)
        String serviceName = traceConfig.getApplications().get(runOrderReqDto.getAppName()).getServiceName();
        //最终项目路径
        String projectFolder = StringUtils.isEmpty(serviceName) ? runOrderReqDto.getCodePath() : runOrderReqDto.getCodePath() + "/" + serviceName;
        File file = new File(projectFolder);
        if (!file.exists()) {
            log.error("File does not exist: " + projectFolder);
            throw new RuntimeException("File does not exist: " + projectFolder);
        }
        //去test下创建mock目录
        String mockFolderPath = projectFolder + SystemConstant.MOCK_FOLDER;
        File mockFolder = new File(mockFolderPath);
        if (!mockFolder.exists()) {
            boolean created = mockFolder.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create directory: " + mockFolder);
            }
        }
        //
        //检查是否有异常日志堆栈
        MessageLogDTO msg = MessageLogUtils.read(mockFolderPath + "/" + SystemConstant.MESSAGE_LOG_NAME);
        if(BooleanUtils.isNotTrue(msg.getIfExceptionStack())){
            //没有异常堆栈, 直接结束
            orderService.modifyStatus(runOrderReqDto.getId(),OrderStatusEnum.NO_EXCEPTION_STACK.getStatus());
            return ;
        }

        //2
        orderService.modifyStatus(runOrderReqDto.getId(),OrderStatusEnum.MOCK_CREATION.getStatus());
        TraceConfig.application application = traceConfig.getApplications().get(runOrderReqDto.getAppName());
        //创建mock执行类
        MockFileUtils.createMockJava(mockFolderPath , runOrderReqDto.getTid(), runOrderReqDto.getAppName(), application.getMainClass(), runOrderReqDto.getDay(),true,application.getElk());

        //3
        //执行mock方法 创建mockTest  MockGpt   MockJdi
        orderService.modifyStatus(runOrderReqDto.getId(),OrderStatusEnum.MOCK_TEST_CREATION.getStatus());
        CmdExecutor.executeCmd( new String[] { "cd "+projectFolder, MVN_COMMAND+" -Dtest="+SystemConstant.MOCK_PACKAGE+"."+SystemConstant.MOCK_INIT_NAME+"#test test"}, traceConfig.getCommandTimeout());



        //编译新生成的java  当前只能通过编译所有的测试代码来实现了
        CmdExecutor.executeCmd( new String[] { "cd "+projectFolder, MVN_COMMAND+" test-compile"}, traceConfig.getCommandTimeout());

        //4运行gpt启动类
        //JvmUtils.startJvm("", MOCK_PACKAGE + "." + SystemConstant.GPT_CLASS_NAME, null, "");
        orderService.modifyStatus(runOrderReqDto.getId(),OrderStatusEnum.MOCK_TEST_RUN.getStatus());
        CmdExecutor.executeCmd( new String[] { "cd "+projectFolder, MVN_COMMAND+" -Dtest="+SystemConstant.MOCK_PACKAGE+"."+SystemConstant.MOCK_INIT_NAME+"#gpt test"}, traceConfig.getCommandTimeout());

        //5读取结果GPT结果,存入order
        String gptAnswerJson = readGptLog(mockFolderPath);
        TraceOrderDTO traceOrderDto = JSON.parseObject(gptAnswerJson, TraceOrderDTO.class);
        if(Objects.isNull(traceOrderDto)){
            throw new RuntimeException("GPT not answer.");
        }
        traceOrderDto.setId(runOrderReqDto.getId());
        traceOrderDto.setStatus(OrderStatusEnum.REPLIED.getStatus());
        orderService.updateByPrimaryKeySelective(traceOrderDto);


    }

    public void createMockJava(String mockFolderPath, RunOrderReqDto runOrderReqDto) {
        try {
            File file = new File(mockFolderPath+ "/"+SystemConstant.MOCK_INIT_NAME+".java");
            FileUtils.deleteQuietly(file);
            PrintWriter writer = new PrintWriter(file);
            writer.println("package mock;");
            writer.println("");
            writer.println("import com.akuchen.trace.parse.dto.QueryLogAndCreateClassFileReq;");
            writer.println("import com.akuchen.trace.parse.mock.MockEnvTest;");
            writer.println("import com.akuchen.trace.parse.utils.JvmUtils;");
            writer.println("import org.junit.Test;");
            writer.println("");
            writer.println("public class "+SystemConstant.MOCK_INIT_NAME+" {");
            writer.println("    @Test");
            writer.println("    public void test(){");
            writer.println("        QueryLogAndCreateClassFileReq req = new QueryLogAndCreateClassFileReq();");
            writer.println("        req.setTid(\"" + runOrderReqDto.getTid() + "\");");
            writer.println("        req.setAppName(\"" + runOrderReqDto.getAppName() + "\");");
            writer.println("        req.setDay(0);");
            writer.println("        req.setOutFilePath(\"" + mockFolderPath + "\");");
            writer.println("        req.setMainClass(\"" + traceConfig.getApplications().get(runOrderReqDto.getAppName()).getMainClass() + "\");");
            writer.println("        req.setWhenParamsIsAny(true);");
            writer.println("        req.setAutorun(false);");
            writer.println("        req.setChatGPTBug(true);");
            writer.println("        req.setChatGPTBugAutorun(false);");
            writer.println("        req.setReport(\"mq\");");
            writer.println("        MockEnvTest.queryLogAndCreateClassFile(req);");
            writer.println("    }");
            writer.println("    @Test");
            writer.println("    public void gpt(){");
            writer.println("        JvmUtils.startJvm(\"\", \"mock."+SystemConstant.GPT_CLASS_NAME+"\", null, \"\");");
            writer.println("    }");
            writer.println("}");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlerMockJava(String projectFolder) {

        try {
            Thread.sleep(5000L);
            ProcessBuilder processBuilder = new ProcessBuilder();
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                processBuilder.command("cmd.exe", "/c", "cd c:"+projectFolder+" ; mvn -Dtest="+SystemConstant.MOCK_INIT_NAME+"#test test");
            }else {
                processBuilder.command("/bin/bash", "-c", "cd "+projectFolder+" && mvn -Dtest="+SystemConstant.MOCK_INIT_NAME+"#test test");
            }
            Process process = processBuilder.start();
//            int exitCode = process.waitFor();
//            log.info("\nExited with error code : " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readGptLog(String mockFolderPath) {
        try {
            File file = new File(mockFolderPath + "/" + SystemConstant.GPT_LOG_NAME );
            if (!file.exists()) {
                log.info("gpt未答复 可能导致的原因1:tid没有异常堆栈,2其他错误");
                return null;
            }
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
