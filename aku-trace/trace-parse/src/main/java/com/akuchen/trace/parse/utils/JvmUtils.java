package com.akuchen.trace.parse.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Optional;

@Slf4j
public class JvmUtils {


    public static void startJvm(String prefixLog,String mainClass,String agentlib,String param) {
        //启动jvm1  业务类
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

        try {
            String cmd=javaBin +" "+ Optional.ofNullable(agentlib).orElse("") + " -cp " + MavenUtils.getMavenDependencies() + " " + mainClass+" "+param;
            log.info(cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            log.info("jvm {} start",prefixLog);
            read(prefixLog,process);
            process.waitFor();
            log.info("jvm {} start success,{}",prefixLog,process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
    private static void read(String prefixLog,Process process) throws IOException {
        InputStream inputStream = process.getInputStream();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
        String line;
        while ((line = inputReader.readLine()) != null) {
            // 处理每行输出
            log.info("{}:{}",prefixLog,line);
        }
    }
}
