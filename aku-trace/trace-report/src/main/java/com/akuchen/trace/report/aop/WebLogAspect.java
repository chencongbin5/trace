//package com.akuchen.service.trace.common.aop;
//
//import com.akuchen.biz.common.config.BizConfigProperties;
//import com.akuchen.biz.common.filter.RequestHeaderHolder;
//import com.akuchen.biz.common.filter.UserRequestHeader;
//import com.akuchen.biz.common.utils.DateTimeUtils;
//import com.akuchen.biz.common.utils.JsonUtils;
//import com.akuchen.biz.common.utils.ListUtils;
//import com.akuchen.biz.common.utils.ObjectUtils;
//import com.akuchen.platform.components.base.util.domain.ApiResult;
//import com.alibaba.fastjson.JSON;
//import lombok.extern.slf4j.Slf4j;
//import org.aopalliance.intercept.MethodInterceptor;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
//import org.springframework.aop.framework.ReflectiveMethodInvocation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//@Configuration
//@Slf4j
//public class WebLogAspect {
//    @Autowired
//    private BizConfigProperties bizConfigProperties;
//
//    @Bean
//    @ConditionalOnProperty(value="trace.enable.web", havingValue = "true")
//    public AspectJExpressionPointcutAdvisor webAdvisor(){
//        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
//        advisor.setExpression("execution(* com..controller..*.*(..))");
//        MethodInterceptor methodInterceptor=initMethodInterceptor();
//        advisor.setAdvice(methodInterceptor);
//        advisor.setOrder(0);
//        return advisor;
//    }
//
//
//    private MethodInterceptor initMethodInterceptor(){
//        MethodInterceptor interceptor = pjp -> {
//            Object[] params = ((ReflectiveMethodInvocation) pjp).getArguments();
//            String requertJson = JSON.toJSONString(params.length == 1 ? params[0] : params);
//            long startMini = System.currentTimeMillis();
//            String classAndMethod = pjp.getMethod().getDeclaringClass().getName() + "." + pjp.getMethod().getName();
//            try {
//                Object result = pjp.proceed();
//                log.info("[trace-general][{}]【request={}】【response={}】[cost:{}ms]",classAndMethod,requertJson,JSON.toJSONString(result),System.currentTimeMillis()-startMini);
//                return result;
//            }catch (Exception e){
//                log.info("[trace-general][{}]【request={}】【response={}】[cost:{}ms]",classAndMethod,requertJson,e.getClass().getName(),System.currentTimeMillis()-startMini);
//                throw  e;
//            }
//        };
//        return interceptor;
//    }
//
//    @Around("execution(* com..controller..*.*(..))")
//    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
//        if (!log.isWarnEnabled()) {
//            return joinPoint.proceed();
//        } else {
//            HttpServletRequest request = ((ServletRequestAttributes)Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
//            String method = request.getMethod();
//            String uri = request.getRequestURI();
//            Stream var10000;
//            if (ListUtils.isNotEmpty(this.bizConfigProperties.getLogExcludedUrl())) {
//                var10000 = this.bizConfigProperties.getLogExcludedUrl().stream();
//                uri.getClass();
//                Optional<String> first = var10000.filter(uri::contains).findFirst();
//                if (first.isPresent()) {
//                    log.info("{} in excluded url config. not trace.", uri);
//                    return joinPoint.proceed();
//                }
//            }
//
//            List<Object> paramList = (List)Stream.of(joinPoint.getArgs()).filter((args) -> {
//                return !(args instanceof ServletRequest);
//            }).filter((args) -> {
//                return !(args instanceof ServletResponse);
//            }).filter(Objects::nonNull).collect(Collectors.toList());
//            String printParamStr = ObjectUtils.isEmpty(paramList) ? null : JsonUtils.toJsonLogOrNull(paramList.get(0));
//            UserRequestHeader userRequestHeader = RequestHeaderHolder.get();
//            String userHeaderStr = JsonUtils.toJsonLogOrNull(userRequestHeader);
//            if (log.isInfoEnabled() && this.bizConfigProperties.isWebLogRequestParams()) {
//                log.info("[{}][{}]【header:{}】【request:{}】", new Object[]{method, uri, userHeaderStr, printParamStr});
//            }
//
//            long startTime = DateTimeUtils.currentMinis();
//            Object result = joinPoint.proceed();
//            long endTime = DateTimeUtils.currentMinis();
//            String printResultStr = JsonUtils.toJsonLogOrNull(result);
//            if (log.isInfoEnabled()) {
//                if (ListUtils.isNotEmpty(this.bizConfigProperties.getLogExcludedResponseUrl())) {
//                    var10000 = this.bizConfigProperties.getLogExcludedResponseUrl().stream();
//                    uri.getClass();
//                    Optional<String> first = var10000.filter(uri::contains).findFirst();
//                    if (first.isPresent()) {
//                        userHeaderStr = "ignored...";
//                        printResultStr = "ignored...";
//                    }
//                }
//
//                log.info("[{}]【header:{}】【response:{}】[rest cost:{}ms]", new Object[]{uri, userHeaderStr, printResultStr, endTime - startTime});
//            } else {
//                ApiResult<Object> result1 = (ApiResult)JsonUtils.parseJsonOrNull(printResultStr, ApiResult.class);
//                if (result1 == null || !result1.isSuccess()) {
//                    log.warn("[{}][{}]【header:{}】【request:{}】【response:{}】", new Object[]{method, uri, userHeaderStr, printParamStr, printResultStr});
//                }
//            }
//
//            return result;
//        }
//    }
//}
