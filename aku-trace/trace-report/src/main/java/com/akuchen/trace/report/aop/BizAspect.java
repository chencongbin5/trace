package com.akuchen.trace.report.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.akuchen.trace.report.TraceLogManager;
import com.akuchen.trace.report.aop.biz.BizSelector;

/**
 * http拦截
 */
@Configuration
@Slf4j
public class BizAspect {

    @Autowired
    private TraceLogManager traceLogManager;



    @Bean
    @ConditionalOnProperty(value="trace.enable", havingValue = "true")
    @ConditionalOnClass(name="org.springframework.web.bind.annotation.RestController")
    //@Conditional(TomcatCondition.class)
    public AspectJExpressionPointcutAdvisor bizAdvisor(BizSelector bizSelector){
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression("execution(* com..controller..*.*(..))");
        advisor.setAdvice(bizSelector.getInterceptor().initMethodInterceptor());
        advisor.setOrder(1);
        return advisor;
    }



}
