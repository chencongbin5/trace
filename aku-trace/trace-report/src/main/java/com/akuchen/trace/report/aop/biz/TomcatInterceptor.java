package com.akuchen.trace.report.aop.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.akuchen.trace.api.common.constant.SystemConstant;
import com.akuchen.trace.report.TraceLogManager;
import com.akuchen.trace.report.log.TraceFileAppender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TomcatInterceptor implements BizInterceptor{


	private TraceLogManager traceLogManager;

	public  TomcatInterceptor(TraceLogManager traceLogManager){
		this.traceLogManager=traceLogManager;
	}

	@Override
	public MethodInterceptor initMethodInterceptor(){
		MethodInterceptor interceptor = pjp -> {
			Object[] params = ((ReflectiveMethodInvocation) pjp).getArguments();
			//String requertJson = JSON.toJSONString(params.length == 1 ? params[0] : params);
			List<Object> serializableParams = new ArrayList<>();
			for (Object param : params) {
				if (!(param instanceof HttpServletResponse) && !(param instanceof HttpServletRequest)) {
					serializableParams.add(param);
				}
			}
			String requertJson = JSON.toJSONString(serializableParams, SerializerFeature.DisableCircularReferenceDetect);
			long startMini = System.currentTimeMillis();
			String classAndMethod = pjp.getMethod().getDeclaringClass().getName() + "." + pjp.getMethod().getName();
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			Map<String, String> headers = getHeadersMap(attributes);
			try {
				Object result = pjp.proceed();
				traceLogManager.info("[trace-biz][{}]【request={}】【headers={}】【response={}】[cost:{}ms]",classAndMethod,requertJson,JSON.toJSONString(headers),JSON.toJSONString(result,SerializerFeature.DisableCircularReferenceDetect),System.currentTimeMillis()-startMini);
				return result;
			}catch (Exception e){
				traceLogManager.info("[trace-biz][{}]【request={}】【headers={}】【response={}】[cost:{}ms]",classAndMethod,requertJson,JSON.toJSONString(headers),e.getMessage(),System.currentTimeMillis()-startMini);
				throw  e;
			}finally {
				ThreadContext.remove(SystemConstant.TRACE_TID);
				TraceFileAppender.contextHolder.remove();
			}
		};
		return interceptor;
	}

	private Map<String, String> getHeadersMap(ServletRequestAttributes attributes) {
		if (attributes != null) {
			HttpServletRequest request = attributes.getRequest();
			Enumeration<String> headerNames = request.getHeaderNames();
			Map<String, String> headers = new LinkedHashMap<>();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				String headerValue = request.getHeader(headerName);
				headers.put(headerName, headerValue);
			}
			return headers;
		}
		return Collections.emptyMap();
	}
}
