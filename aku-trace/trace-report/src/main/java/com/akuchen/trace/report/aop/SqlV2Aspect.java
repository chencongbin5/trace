package com.akuchen.trace.report.aop;

import com.akuchen.trace.report.TraceLogManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSession.StrictMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = ResultHandler.class, method = "handleResult", args = {ResultContext.class})
})
@Component
@Slf4j
@ConditionalOnProperty(value = "trace.enable", havingValue = "true")
@ConditionalOnClass(name="org.apache.ibatis.executor.Executor")
public class SqlV2Aspect implements Interceptor {


    @Autowired
    private TraceLogManager traceLogManager;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startMini = System.currentTimeMillis();
        try {
            Object proceed = invocation.proceed();
            log(invocation, proceed, startMini);
            return proceed;
        } catch (Exception e) {
            log(invocation, e, startMini);
            throw e;
        }

    }

    private void log(Invocation invocation, Object proceed, long startMini) {
        try {
            //类和方法
            String classAndMethod = ((MappedStatement) invocation.getArgs()[0]).getId();

            //入参
            List<Object> params = new ArrayList<>();
            List<String> paramTypes = new ArrayList<>();

            if (invocation.getArgs()[1] instanceof MapperMethod.ParamMap) {
                MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) invocation.getArgs()[1];
                for (int i = 0; i < paramMap.size() / 2; i++) {
                    Object o = paramMap.get("param" + (i + 1));
                    params.add(o);
                    paramTypes.add(Objects.isNull(o)?null:paramMap.get("param" + (i + 1)).getClass().getName());
                }
            }else if(invocation.getArgs()[1]  instanceof StrictMap){
                StrictMap StrictMap = (StrictMap)invocation.getArgs()[1];
                if(StrictMap.get("collection")!=null && StrictMap.get("list")!=null){
                    Object o = StrictMap.get("list");
                    params.add(o);
                    paramTypes.add(Objects.isNull(o)?null:StrictMap.get("list").getClass().getName());
                }
            }
            else {
                Object o = invocation.getArgs()[1];
                params.add(o);
                paramTypes.add(Objects.isNull(o)?null:invocation.getArgs()[1].getClass().getName());
            }
            //param1 param2 param3


            //出参
            //1.抛异常
            if (proceed instanceof Exception) {
                traceLogManager.info("[trace-general][{}]【request={}】【response={}】[cost:{}ms][paramType:{}][returnType:{}]",
                        classAndMethod,
                        JSON.toJSONString(params, SerializerFeature.DisableCircularReferenceDetect),
                        proceed.getClass().getName(),
                        System.currentTimeMillis() - startMini,
                        JSON.toJSONString(paramTypes, SerializerFeature.DisableCircularReferenceDetect),
                        null);
                return;
            }
            //2.正常情况
            String returnType = proceed.getClass().getName();
            //如果returnType=java.util.ArrayList 我们需要另外取出集合中的对象类型 map什么的就暂时不管了,稀有场景
            if (proceed instanceof ArrayList && ((ArrayList) proceed).size() > 0 && ((ArrayList) proceed).get(0) != null) {
                String className = ((ArrayList) proceed).get(0).getClass().getName();
                returnType = returnType + "<" + className + ">";
            }
            //入参出参有可能是泛型,事后解析压根不知道里面一开始是啥,所以需要提前输出
            traceLogManager.info("[trace-general][{}]【request={}】【response={}】[cost:{}ms][paramType={}][returnType={}]",
                    classAndMethod,
                    JSON.toJSONString(params, SerializerFeature.DisableCircularReferenceDetect),
                    JSON.toJSONString(proceed, SerializerFeature.DisableCircularReferenceDetect),
                    System.currentTimeMillis() - startMini,
                    JSON.toJSONString(paramTypes, SerializerFeature.DisableCircularReferenceDetect),
                    returnType);
        } catch (Exception e) {
            log.error("trace-general print error :", e);
        }

    }

    @Override
    public Object plugin(Object target) {
        //判断是否拦截这个类型对象（根据@Intercepts注解决定），然后决定是返回一个代理对象还是返回原对象。
        //故我们在实现plugin方法时，要判断一下目标类型，如果是插件要拦截的对象时才执行Plugin.wrap方法，否则的话，直接返回目标本身。
        if (target instanceof Executor || target instanceof ResultHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
