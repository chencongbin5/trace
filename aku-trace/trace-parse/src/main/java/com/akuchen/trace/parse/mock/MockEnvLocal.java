//package com.akuchen.trace.parse.mock;
//
//import com.akuchen.trace.parse.builder.MethodMockBuilder;
//import com.akuchen.trace.parse.dto.CodeInfo;
//import com.akuchen.trace.parse.dto.QueryLogAndCreateClassFileReq;
//import com.akuchen.trace.parse.dto.TemplateReq;
//import com.akuchen.trace.parse.utils.EsQueryUtils;
//import com.akuchen.trace.parse.utils.ListUtils;
//import com.akuchen.trace.parse.utils.ValUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.io.FileUtils;
//import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;
//import org.junit.runner.notification.Failure;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
///**
// * 本机mock
// * 本机运行没mock的@test,执行日志, 手动拷贝到指定路径 生成日志文件 模拟测试环境调用
// * 根据日志文件 执行下面queryLogAndCreateClassFile 方法 生成新的@test
// * 减少 修改一个点 然后去测试环境执行 才能验证queryLogAndCreateClassFile的修改结果
// */
//@Slf4j
//public class MockEnvLocal {
//
//
//    /**
//     *
//     */
//    public static void queryLogAndCreateClassFile(QueryLogAndCreateClassFileReq req) {
//        try {
//            File logFile = new File(req.getOutFilePath()+"/local.log");
//            File mockStart = new File(req.getOutFilePath()+"/TestMock.java");
//            log.info("0 初始化");
//            log.info("1 查日志");
//            List<String> messages = FileUtils.readLines(logFile, "UTF-8");
//            log.info("1 查日志 行数:{}", messages.size());
//            if (CollectionUtils.isEmpty(messages)) {
//                log.info("没有日志");
//                return;
//            }
//            //日志手动排序
//            //在elastic中排序字段用的是@timestamp ,filebeat做的数据同步 这应该是同步的时间, 存在多条数据值相同的情况, 目前也没条件去那边加时间字段,手动把拉下来的数据 做个排序,可能存在日志格式不同导致排序失败的情况
//            List<String> sortMessages = ListUtils.sortByDate(messages);
//
//
//            FileUtils.writeLines(logFile, sortMessages);
//            //sortMessages = ValUtils.replace(sortMessages);
//            log.info("2 生成mock代码");
//            List<CodeInfo> codeInfos = MethodMockBuilder.start(sortMessages,req.isWhenParamsIsAny());
//            log.info("3 生成.java代码");
//            String s = req.getOutFilePath().replaceAll("\\\\|/", ".");
//            String packageName = s.substring(s.indexOf("test.java") + 10);
//            String resut = MethodMockBuilder.template(TemplateReq.builder()
//                    .className("TestMock")
//                    .codeInfos(codeInfos)
//                    .packageName(packageName)
//                    .env("test")
//                    .mainClass(req.getMainClass())
//                    .build());
//            FileUtils.write(mockStart, resut);
//
//            log.info("4 ----------------file-create--success------------------------------------");
//            if(req.isAutorun()){
//                log.info("5. 开始运行mock测试");
//                //启动mock服务
//                Result result = JUnitCore.runClasses(Class.forName(packageName+".TestMock"));
//                for (Failure failure : result.getFailures()) {
//                    System.out.println(failure.toString());
//                }
//            }
//
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//}
