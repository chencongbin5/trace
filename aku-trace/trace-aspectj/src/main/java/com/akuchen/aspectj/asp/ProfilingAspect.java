//package com.akuchen.aspectj.asp;
//
//import com.alibaba.fastjson.JSON;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//
//@Aspect
//@Slf4j
//public class ProfilingAspect {
//
//    @Pointcut("execution(* com.al.inf.leaf.service.LeafService.genId(..))")
//    public void modelLayer() {
//    }
//
//    @Around("modelLayer()")
//    public Object logProfile(ProceedingJoinPoint joinPoint) throws Throwable {
//        Object result = joinPoint.proceed();
//        Signature signature = joinPoint.getSignature();
//        Object[] params = joinPoint.getArgs();//参数 null
//        String requertJson = JSON.toJSONString(params.length == 1 ? params[0] : params);
//        log.info("[trace-general]["+ signature.getDeclaringTypeName()+"."+signature.getName()+"]【request="+ requertJson +"】【response="+JSON.toJSONString(result)+"】");
//
//        return result;
//
//    }
//}
