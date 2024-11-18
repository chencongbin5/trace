package com.akuchen.trace.parse.mock;

import com.akuchen.trace.api.common.constant.SystemConstant;
import com.akuchen.trace.api.common.enums.ElkEnum;
import com.akuchen.trace.api.common.enums.SendTypeEnum;
import com.akuchen.trace.api.common.utils.MessageLogUtils;
import com.akuchen.trace.parse.builder.GptClassBuilder;
import com.akuchen.trace.parse.builder.JdiClassBuilder;
import com.akuchen.trace.parse.builder.MockClassBuilder;
import com.akuchen.trace.parse.builder.template.MockTest;
import com.akuchen.trace.parse.builder.template.MockTestNoMock;
import com.akuchen.trace.parse.builder.template.MockTestSleep;
import com.akuchen.trace.parse.dto.CodeInfoDTO;
import com.akuchen.trace.parse.dto.QueryLogAndCreateClassFileReq;
import com.akuchen.trace.parse.dto.TemplateReq;
import com.akuchen.trace.parse.http.EsQueryHttp;
import com.akuchen.trace.parse.http.TraceQueryHttp;
import com.akuchen.trace.parse.utils.JvmUtils;
import com.akuchen.trace.parse.utils.ListUtils;
import com.akuchen.trace.parse.utils.ObjUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试环境mock
 */
@Slf4j
public class MockEnvTest {

    private static final String MOCK_CLASS_NAME = "MockTest";
    private static final String JDI_CLASS_NAME = "MockJdi";
    private static final String GPT_CLASS_NAME = "MockGpt";

    public static void queryLogAndCreateClassFile(QueryLogAndCreateClassFileReq req) {
        try {
            String path = req.getOutFilePath().replaceAll("\\\\", "/");
            File logFile = new File(path + "/tid.log");
            File messageFile = new File(path + "/"+ SystemConstant.MESSAGE_LOG_NAME);
            File gptlogFile = new File(path + "/"+SystemConstant.GPT_LOG_NAME);
            File mockFile = new File(path + "/" + MOCK_CLASS_NAME + ".java");
            File jdiFile = new File(path + "/" + JDI_CLASS_NAME + ".java");
            File gptFile = new File(path + "/" + GPT_CLASS_NAME + ".java");
            log.info("0 初始化");
            FileUtils.deleteQuietly(gptlogFile);
            FileUtils.deleteQuietly(messageFile);
            //FileUtils.deleteQuietly(mockFile);
            FileUtils.deleteQuietly(jdiFile);
            FileUtils.deleteQuietly(gptFile);
            FileUtils.deleteQuietly(logFile);
            ObjUtils.deleteFilesWithPrefix(path,MOCK_CLASS_NAME);
            ObjUtils.deleteFilesWithPrefix(path,"json_");
            //提前设置,,给后面使用
            MessageLogUtils.setFile(messageFile);

            log.info("1 查日志");
            List<String> messages = queryLog(req);
            log.info("1 查日志 行数:{}", messages.size());
            if (CollectionUtils.isEmpty(messages)) {
                log.info("没有日志");
                return;
            }
            FileUtils.writeLines(logFile, messages);
            //sortMessages = ValUtils.replace(sortMessages);
            log.info("2 生成mock代码");
            List<CodeInfoDTO> codeInfoDTOS = MockTest.start(messages, req);
            List<CodeInfoDTO> codeInfoNoMockDTOS = MockTestNoMock.start(messages, req);
            log.info("3 生成.java代码");
            String s = path.replaceAll("\\\\|/", ".");
            String packageName = s.substring(s.indexOf("test.java") + 10);

            TemplateReq templateReq = TemplateReq.builder()
                    .className(MOCK_CLASS_NAME)
                    .jdiClassName(JDI_CLASS_NAME)
                    .gptClassName(GPT_CLASS_NAME)
                    .codeInfoDTOS(codeInfoDTOS)
                    .packageName(packageName)
                    .env("test")
                    .mainClass(req.getMainClass())
                    .build();

            //生成MockTest测试类
            templateReq.setCodeInfoDTOS(codeInfoDTOS);
            FileUtils.write(new File(path + "/" + MOCK_CLASS_NAME + ".java"), MockTest.template(templateReq,""));
            //生成mocktestSleep测试类
            templateReq.setCodeInfoDTOS(codeInfoDTOS);
            FileUtils.write(new File(path + "/" + MOCK_CLASS_NAME + "Sleep.java"), MockTestSleep.template(templateReq,"Sleep"));
            //生成mockTestNoMock测试类
            templateReq.setCodeInfoDTOS(codeInfoNoMockDTOS);
            FileUtils.write(new File(path + "/" + MOCK_CLASS_NAME + "NoMock.java"), MockTestNoMock.template(templateReq,"NoMock"));

            log.info("4 ---------------mock-file-create--success------------------------------------");

            if (req.isAutorun()) {
                log.info("5. 开始运行mock测试");
                //启动mock服务
                Result result = JUnitCore.runClasses(Class.forName(packageName + "." + MOCK_CLASS_NAME));
                for (Failure failure : result.getFailures()) {
                    log.info(failure.toString());
                }
            }
            //关闭自动启动才能检查用gpt检查bug
            if (!req.isAutorun() && BooleanUtils.isTrue(req.getChatGPTBug())) {
                //一次请求可能存在多个抛异常, 有的被catch后被降级了 ,有的抛异常但是不打紧,具体哪些异常要检查,有哪些异常可以忽略 我还没想好,这里就暂时先只处理最后一个异常堆栈,来日方长
                //启动jdi监听程序


                log.info("6. 开始运行gpt检查BUG原因(适用于有抛异常的tid)");
                FileUtils.write(jdiFile, JdiClassBuilder.template(templateReq, path + "/"+SystemConstant.GPT_LOG_NAME));
                log.info(" ----------------jdi-file-create--success------------------------------------");
                FileUtils.write(gptFile, GptClassBuilder.template(templateReq));
                log.info(" ----------------GPT-file-create--success------------------------------------");
                if (BooleanUtils.isTrue(req.getChatGPTBugAutorun())) {
                    log.info("5. 开始运行MockGpt");
//                    Result result = JUnitCore.runClasses(Class.forName(packageName+"."+GPT_CLASS_NAME));
//                    for (Failure failure : result.getFailures()) {
//                        log.info(failure.toString());
//                    }
                    JvmUtils.startJvm("", packageName + "." + GPT_CLASS_NAME, null, "");
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> queryLog(QueryLogAndCreateClassFileReq req) {
        List<String> result=new ArrayList<>();
        if (SendTypeEnum.isMq(req.getReport())) {
            //这里拿trace日志
            List<String> traceLogs = TraceQueryHttp.queryLog(req.getTid(), req.getAppName());
            //这里拿普通日志
            if (CollectionUtils.isNotEmpty(traceLogs)){
                result.add("-----------------------------------------trace上报信息---------------------------------------------------------------------");
                result.add(" ");
                result.addAll(traceLogs);
            }

        }
        List<String> commonLogs = null;
        ElkEnum elkEnum = ElkEnum.getEnum(req.getElk());
		try {
            commonLogs =(List<String>)
                    Class.forName(elkEnum.getClazz()).getMethod("queryLog", Integer.class, String.class, String.class)
                         .invoke(null, req.getDay(),req.getTid(), req.getAppName());
        } catch (Exception e) {
			e.printStackTrace();
		}

		//List<String> commonLogs = EsQueryHttp.queryLog(req.getDay(), req.getTid(), req.getAppName());
        //日志手动排序
        //在elastic中排序字段用的是@timestamp ,filebeat做的数据同步 这应该是同步的时间, 存在多条数据值相同的情况, 目前也没条件去那边加时间字段,手动把拉下来的数据 做个排序,可能存在日志格式不同导致排序失败的情况
        if(CollectionUtils.isNotEmpty(commonLogs)){
            result.add("-----------------------------------------elk日志---------------------------------------------------------------------");
            result.add(" ");
            List<String> sortMessages = ListUtils.sortByDate(commonLogs);
            result.addAll(sortMessages);
        }
        return result;
    }


}
