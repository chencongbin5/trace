package com.akuchen.trace.report.log.common;

/**
 * 功能1
 * 把错误日志用http的方式上报到服务端
 */
public class TraceLogging {

	private boolean isLogback = false;

	private TraceAppender traceAppender;

	public TraceLogging() {
		try {
			Class.forName("ch.qos.logback.classic.Logger");
			traceAppender=new TraceLogBackAppender();
			isLogback = true;
		} catch (ClassNotFoundException e) {
			traceAppender = new TraceLog4j2Appender("traceAppender");
		}
		finally {
			traceAppender.load();
		}
	}

	public TraceAppender getTraceAppender() {
		return traceAppender;
	}
}
