package com.akuchen.trace.report.log.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.akuchen.trace.report.TraceLogManager;
import com.akuchen.trace.report.log.TraceLogger;

public class TraceLog4j2Appender extends AbstractAppender implements TraceAppender {


	protected TraceLog4j2Appender(String name) {
		super(name, null, null);
	}

	@Override
	public void append(LogEvent event) {
		if (event.getThrown() != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(event.getThrownProxy().toString());
			sb.append("\r\n");
			for (StackTraceElement element : event.getThrown().getStackTrace()) {
				sb.append("\tat "+element.toString());
				sb.append("\r\n");
			}
			TraceLogger.log(TraceLogManager.applicationName, sb.toString());
		}

//		if (event.getThrown() != null) {
//			// 在这里处理异常
//			TraceLogger.log(TraceLogManager.applicationName, event.getThrown().toString());
//		}
	}

	@Override
	public void load() {
		org.apache.logging.log4j.core.LoggerContext
				ctx = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();

		this.start();
		config.addAppender(this);

		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		loggerConfig.addAppender(this, null, null);
		ctx.updateLoggers();
	}

	//	@Override
//	public void append(LogEvent logEvent) {
//		if (logEvent.getThrown() != null) {
//			// 执行特定的方法
//			StringWriter stringWriter = new StringWriter();
//			PrintWriter printWriter = new PrintWriter(stringWriter);
//			logEvent.getThrown().printStackTrace(printWriter);
//			String stackTrace = stringWriter.toString();
//			TraceLogger.log(TraceLogManager.applicationName,stackTrace);
//		}
//	}

}
