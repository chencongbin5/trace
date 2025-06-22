package com.akuchen.trace.report.aop.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.web.server.ServerWebExchange;

import com.akuchen.trace.api.common.constant.SystemConstant;
import com.akuchen.trace.report.TraceLogManager;
import com.akuchen.trace.report.log.TraceFileAppender;

import java.util.ArrayList;
import java.util.List;

/**
 * webflux 真是难受啊 , 不搞不搞他了
 */
@Slf4j
public class WebfluxInterceptor implements BizInterceptor {


	private TraceLogManager traceLogManager;

	public  WebfluxInterceptor(TraceLogManager traceLogManager){
		this.traceLogManager=traceLogManager;
	}


	@Override
	public MethodInterceptor initMethodInterceptor(){
		MethodInterceptor interceptor = pjp -> {
			Object[] params = ((ReflectiveMethodInvocation) pjp).getArguments();
			List<Object> serializableParams = new ArrayList<>();
			for (Object param : params) {
				if (!(param instanceof ServerWebExchange)) {
					serializableParams.add(param);
				}
			}
			String requertJson = JSON.toJSONString(serializableParams, SerializerFeature.DisableCircularReferenceDetect);
			long startMini = System.currentTimeMillis();
			String classAndMethod = pjp.getMethod().getDeclaringClass().getName() + "." + pjp.getMethod().getName();

			try {
				Object result = pjp.proceed();
				traceLogManager.info("[trace-biz][{}]【request={}】【headers=null】【response={}】[cost:{}ms]",classAndMethod,requertJson,JSON.toJSONString(result,SerializerFeature.DisableCircularReferenceDetect),System.currentTimeMillis()-startMini);
				return result;
			}catch (Exception e){
				traceLogManager.info("[trace-biz][{}]【request={}】【headers=null】【response={}】[cost:{}ms]",classAndMethod,requertJson,e.getMessage(),System.currentTimeMillis()-startMini);
				throw  e;
			}finally {
				ThreadContext.remove(SystemConstant.TRACE_TID);
				TraceFileAppender.contextHolder.remove();
			}
		};
		return interceptor;
	}

}
