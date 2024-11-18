package com.akuchen.trace.report.aop.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.akuchen.trace.report.TraceLogManager;


@Configurable
public class BizSelector {

	private BizInterceptor interceptor;


	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext,TraceLogManager traceLogManager) {

		if (applicationContext instanceof ReactiveWebApplicationContext) {
			//-- WebFlux --
			interceptor=new WebfluxInterceptor(traceLogManager);
		} else if (applicationContext instanceof GenericWebApplicationContext) {
			//-- Servlet tomcat--
			interceptor=new TomcatInterceptor(traceLogManager);
		}
	}

	public BizInterceptor getInterceptor() {
		return interceptor;
	}

}
