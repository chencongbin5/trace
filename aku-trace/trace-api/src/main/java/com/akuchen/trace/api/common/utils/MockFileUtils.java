package com.akuchen.trace.api.common.utils;

import com.akuchen.trace.api.common.constant.SystemConstant;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.PrintWriter;

public class MockFileUtils {
    /**
     * 创建mock文件 共用
     * @param mockFolderPath
     * @param tid
     * @param appName
     * @param mainClass
     * @param day
     */
    public static void createMockJava(String mockFolderPath,String tid,String appName,String mainClass,Integer day,Boolean chatGPTBug,String elk) {
        try {
            if (StringUtils.isNotEmpty(elk)){
                elk="aku";
            }

            File file = new File(mockFolderPath+ "/"+ SystemConstant.MOCK_INIT_NAME+".java");
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
            writer.println("        req.setTid(\"" + tid + "\");");
            writer.println("        req.setAppName(\"" + appName + "\");");
            writer.println("        req.setDay("+day+");");
            writer.println("        req.setOutFilePath(\"" + mockFolderPath + "\");");
            writer.println("        req.setMainClass(\"" + mainClass + "\");");
            writer.println("        req.setWhenParamsIsAny(true);");
            writer.println("        req.setAutorun(false);");
            writer.println("        req.setChatGPTBug("+chatGPTBug+");");
            writer.println("        req.setChatGPTBugAutorun(false);");
            writer.println("        req.setElk(\""+elk+"\");");
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
}
