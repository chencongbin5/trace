package com.akuchen.trace.report.aop;

import com.akuchen.trace.report.TraceLogManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * leaf id生成器拦截
 */
@Configuration
@Slf4j
public class LeafAspect {


    @Autowired
    private TraceLogManager traceLogManager;

    @Bean
    @ConditionalOnProperty(value="trace.enable", havingValue = "true")
    @ConditionalOnClass(name = {"com.al.inf.leaf.service.LeafService"})
    public AspectJExpressionPointcutAdvisor leafAdvisor(){
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression("execution(* com.al.inf.leaf.service.LeafService.genId(..))");
        advisor.setAdvice(initMethodInterceptor());
        advisor.setOrder(1);
        return advisor;
    }


    private MethodInterceptor initMethodInterceptor(){
        MethodInterceptor interceptor = pjp -> {
            Object[] params = ((ReflectiveMethodInvocation) pjp).getArguments();
            //String requertJson = JSON.toJSONString(params.length == 1 ? params[0] : params);
            String requertJson = JSON.toJSONString(params, SerializerFeature.DisableCircularReferenceDetect);
            long startMini = System.currentTimeMillis();
            String classAndMethod = pjp.getMethod().getDeclaringClass().getName() + "." + pjp.getMethod().getName();
            try {
                Object result = pjp.proceed();
                traceLogManager.info("[trace-general][{}]【request={}】【response={}】[cost:{}ms]",classAndMethod,requertJson,JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect),System.currentTimeMillis()-startMini);
                return result;
            }catch (Exception e){
                traceLogManager.info("[trace-general][{}]【request={}】【response={}】[cost:{}ms]",classAndMethod,requertJson,e.getMessage(),System.currentTimeMillis()-startMini);
                throw  e;
            }
        };
        return interceptor;
    }

}
