package com.akuchen.trace.report.filter;


import com.akuchen.trace.api.common.constant.SystemConstant;
import com.akuchen.trace.report.TraceLogManager;
import com.akuchen.trace.report.log.TraceFileAppender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.logging.log4j.ThreadContext;

import java.text.MessageFormat;
import java.util.Objects;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;


@Slf4j
@Activate(group = PROVIDER, order = 10000)
public class ProviderRpcLogFilter implements Filter {

    public static final String RESOURCE_NAME_FORMATTER_PATTERN = "{0}.{1}";
    public static final String LOG_FORMATTER_REQUEST_PATTERN = "[trace-rpc-provider][{}]【request={}】";
    public static final String LOG_FORMATTER_RESPONSE_PATTERN = "[trace-rpc-provider][{}]【request={}】【response={}】[cost:{}ms]";


    private TraceLogManager traceLogManager;

    public void setTraceLogManager(TraceLogManager traceLogManager) {
        this.traceLogManager = traceLogManager;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
		Boolean isCreateTid = null;
		try {
			String serviceName = invoker.getInterface().getCanonicalName();
			String methodName = invocation.getMethodName();
			String resourceName = MessageFormat.format(RESOURCE_NAME_FORMATTER_PATTERN, serviceName, methodName);
			//String requestParam = invocation.getArguments().length == 1 ? JSON.toJSONString(invocation.getArguments()[0]): JSON.toJSONString(invocation.getArguments());
			String requestParam =
					JSON.toJSONString(invocation.getArguments(), SerializerFeature.DisableCircularReferenceDetect);
			//记录请求入参到上下文
			isCreateTid = traceLogManager.info(LOG_FORMATTER_REQUEST_PATTERN, resourceName, requestParam);
			long startMini = System.currentTimeMillis();
			Result invoke = invoker.invoke(invocation);
			long endMini = System.currentTimeMillis();
			Object realResponseObj = ((AsyncRpcResult) invoke).getAppResponse().getValue();
			String resultJson = JSON.toJSONString(realResponseObj, SerializerFeature.DisableCircularReferenceDetect);
			resultJson = Objects.isNull(resultJson) && Objects.nonNull(realResponseObj) ?
						 String.valueOf(realResponseObj) :
						 resultJson;
			traceLogManager.info(LOG_FORMATTER_RESPONSE_PATTERN,
					resourceName,
					requestParam,
					resultJson,
					endMini - startMini);
			return invoke;
		} catch (Exception e) {
			throw e;
		} finally {
			//如果有创建tid 清理上下文 1打印的traceTid  2threadlocal信息
			//其他入口都不需要加isCreateTid 判断, 唯独dubbo的入口, 因为dubbo接口可以本地调用
			if (BooleanUtils.isTrue(isCreateTid)) {
                ThreadContext.remove(SystemConstant.TRACE_TID);
                TraceFileAppender.contextHolder.remove();
			}
		}
	}


}
