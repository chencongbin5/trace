//package com.akuchen.trace.parse;
//
//import com.akuchen.trace.parse.jdi.JdiMethodManager;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class JdiTest {
//
//    public static void main(String[] args) {
//        JdiMethodManager manager = JdiMethodManager.getInstance();
//                //执行类    方法咱就固定main了
//        manager  .setTestClassName("com.akuchen.trace.parse.ccb");
//                //打debugger的位置  类,方法,行;
////        manager     .setDebuggerClasss("com.akuchen.trace.parse.ccb");
////        manager    .setDebuggerMethod("main");
////        manager      .setDebuggerLine(5);
//        try {
//            manager.run();
//        } catch (Exception e) {
//            log.error(e.getMessage(),e);
//        }
//    }
//}