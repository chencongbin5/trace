package com.akuchen.trace.report.filter;


import com.akuchen.trace.report.TraceLogManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.text.MessageFormat;
import java.util.Objects;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

@Slf4j
@Activate(group = CONSUMER, order = 10000)
public class ConsumerRpcLogFilter implements Filter {

    public static final String RESOURCE_NAME_FORMATTER_PATTERN = "{0}.{1}";
    public static final String LOG_FORMATTER_PATTERN = "[trace-rpc][{}]【request={}】【response={}】[cost:{}ms]";
    public static final String RESOURCE_LOG_FORMATTER_PATTERN = "[trace-rpc][{0}]【request={1}】【response={2}】[cost:{3}ms]";

    //public static final SerializerFeature[] JSON_FORMATTER_SERIALIZER_FEATURES = new SerializerFeature[]{SerializerFeature.WriteNonStringKeyAsString};

    private TraceLogManager traceLogManager;

    public void setTraceLogManager(TraceLogManager traceLogManager) {
        this.traceLogManager = traceLogManager;
    }





    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        String serviceName = invoker.getInterface().getCanonicalName();
        String methodName = invocation.getMethodName();
        String resourceName = MessageFormat.format(RESOURCE_NAME_FORMATTER_PATTERN, serviceName, methodName);
        long startMini = System.currentTimeMillis();

        //String requestParam = invocation.getArguments().length == 1 ? JSON.toJSONString(invocation.getArguments()[0]): JSON.toJSONString(invocation.getArguments());
        String requestParam = JSON.toJSONString(invocation.getArguments(), SerializerFeature.DisableCircularReferenceDetect);
        Result innerResult = this.invokeWithException(invoker, invocation, resourceName, requestParam, startMini);
        long endMini = System.currentTimeMillis();
        Object realResponseObj = ((AsyncRpcResult) innerResult).getAppResponse().getValue();
        String resultJson = JSON.toJSONString(realResponseObj, SerializerFeature.DisableCircularReferenceDetect);
        resultJson = Objects.isNull(resultJson) && Objects.nonNull(realResponseObj) ? String.valueOf(realResponseObj) : resultJson;
        traceLogManager.info(LOG_FORMATTER_PATTERN, new Object[]{resourceName, requestParam, resultJson, endMini - startMini});

        return innerResult;

    }

    private Result invokeWithException(Invoker<?> invoker, Invocation invocation, String resourceName, String requestParam, long startMini) {
        try {
            return invoker.invoke(invocation);
        } catch (RpcException var6) {
            traceLogManager.info(MessageFormat.format(RESOURCE_LOG_FORMATTER_PATTERN, resourceName, requestParam, var6.getClass().getName(), System.currentTimeMillis() - startMini), var6);
            throw var6;
        }
    }



}
