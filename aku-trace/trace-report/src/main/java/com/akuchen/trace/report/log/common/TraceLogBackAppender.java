package com.akuchen.trace.report.log.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.impl.StaticLoggerBinder;

import com.akuchen.trace.report.TraceLogManager;
import com.akuchen.trace.report.log.TraceLogger;

public class TraceLogBackAppender extends AppenderBase<ILoggingEvent>  implements TraceAppender {


	@Override
	protected void append(ILoggingEvent logEvent) {
		if (logEvent.getThrowableProxy() != null) {
			// 当日志输出异常堆栈的时候，执行特定方法
			TraceLogger.log(TraceLogManager.applicationName, ThrowableProxyUtil.asString(logEvent.getThrowableProxy()));
		}
	}

	@Override
	public void load() {
		LoggerContext loggerContext = (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
		ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		this.setName("traceAppender");
		this.setContext(loggerContext);
		this.start();
		rootLogger.addAppender(this);
	}
}
