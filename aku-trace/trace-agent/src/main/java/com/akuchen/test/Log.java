//package com.akuchen.test;
//
//import com.akuchen.trace.report.log.TraceLogger;
//
//public class Log {
//
//	public String info(String string){
//		TraceLogger.log("application-test","[trace-report][param={}]",string);
//		System.out.println("log:"+string);
//		String result= string+1;
//		TraceLogger.log("application-test","[trace-report][param={}][response={}]",string,result);
//		return result;
//	}
//
//	public String info2(String string){
//		System.out.println("log:"+string);
//		String result= string+1;
//		return result;
//	}
//}
