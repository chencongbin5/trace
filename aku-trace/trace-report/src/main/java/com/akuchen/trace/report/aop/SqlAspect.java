package com.akuchen.trace.report.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * sql拦截
 * @author chencb
 */
@Configuration
@Slf4j
public class SqlAspect {

    @Value("${trace.sqlPointcut:}")
    private String sqlPointcut;

    /**
     * @ConditionalOnProperty  trace.enable配置存在 并且为true  执行该方法
     * @return
     */
//    @Bean
//    @ConditionalOnProperty(value="trace.enable.sql", havingValue = "true")
//    public AspectJExpressionPointcutAdvisor sqlAdvisor(){
//        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
//        advisor.setExpression(sqlPointcut);
//        advisor.setAdvice(initMethodInterceptor());
//        advisor.setOrder(0);
//        return advisor;
//    }


    private MethodInterceptor initMethodInterceptor(){
        MethodInterceptor interceptor = pjp -> {
            Object[] params = ((ReflectiveMethodInvocation) pjp).getArguments();
            //String requertJson = JSON.toJSONString(params.length == 1 ? params[0] : params);
            String requertJson = JSON.toJSONString(params, SerializerFeature.DisableCircularReferenceDetect);
            long startMini = System.currentTimeMillis();
            String classAndMethod = pjp.getMethod().getDeclaringClass().getName() + "." + pjp.getMethod().getName();
            try {
                Object result = pjp.proceed();
                log.info("[trace-general][{}]【request={}】【response={}】[cost:{}ms]",classAndMethod,requertJson,JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect),System.currentTimeMillis()-startMini);
                return result;
            }catch (Exception e){
                log.info("[trace-general][{}]【request={}】【response={}】[cost:{}ms]",classAndMethod,requertJson,e.getClass().getName(),System.currentTimeMillis()-startMini);
                throw  e;
            }
        };
        return interceptor;
    }
}
