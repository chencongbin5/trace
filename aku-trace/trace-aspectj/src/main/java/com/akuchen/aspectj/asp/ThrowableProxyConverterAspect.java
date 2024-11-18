//package com.akuchen.aspectj.asp;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//
///**
// * logback 打印堆栈的处理   成功了,但没有使用场景,注释
// */
//@Aspect
//@Slf4j
//public class ThrowableProxyConverterAspect {
//
//    private final String pointcut = "execution(* ch.qos.logback.classic.pattern.ThrowableProxyConverter.throwableProxyToString(..))";
//
//    @Pointcut(pointcut)
//    public void restTemplate() {
//    }
//
//    @Around("restTemplate()")
//    public Object logRestTemplate(ProceedingJoinPoint joinPoint) throws Throwable {
////        Signature signature = joinPoint.getSignature();
////        Object[] params = joinPoint.getArgs();
//        try {
//
//            Object result = joinPoint.proceed();
//            result="\t"+result;
//            return result;
//        }catch (Exception e){
//            throw  e;
//        }
//
//    }
//}
