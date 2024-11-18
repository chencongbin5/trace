//package com.akuchen.trace.parse;
//
//import com.akuchen.trace.parse.utils.MavenUtils;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.util.Arrays;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class Start {
//    /**
//     * 因为业务类通过idea启动的时候会自带agent idea_rt.jar包 好像会影响我的agentlib:jdwp 连接,这里验证下是否的确如此
//     * @param args
//     */
//    public static void main(String[] args) {
//
//        //启动jvm1 业务类
//        startJvm("com.akuchen.trace.parse.ccb","agentlib:jdwp=transport=dt_socket,address=127.0.0.1:5005,suspend=y,server=n");
//        //启动jvm2 调试类
//        //startJvm("com.akuchen.trace.parse.JdiTest",null);
//        // 检查调试类是否连接业务类成功
//
//    }
//
//    public static void startJvm(String mainClass,String agentlib) {
//        //启动jvm1  业务类
//        String javaHome = System.getProperty("java.home");
//        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
//        String classpath = "/path/to/your/app.jar";
//
//        try {
//            Process process = Runtime.getRuntime().exec(javaBin +" "+ Optional.ofNullable(agentlib).orElse("") + " -cp " + MavenUtils.getMavenDependencies() + " " + mainClass);
//            log.info("jvm start ");
//            process.waitFor();
//            log.info("jvm start success");
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//}
