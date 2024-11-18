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
// * http 拦截 日志打印
// * 直接拦截相对比较底层的request.execute()方法,缺点是入参为空, 如果一个线程由多次外部接口调用, 顺序一定不能乱,否则返回结果不对
// */
//@Aspect
//@Slf4j
//public class RestTemplateAspect {
//
//    private final String pointcut = "execution(* org.springframework.http.client.ClientHttpRequest.execute(..))";
//
//    @Pointcut(pointcut)
//    public void restTemplate() {
//    }
//
//    @Around("restTemplate()")
//    public Object logRestTemplate(ProceedingJoinPoint joinPoint) throws Throwable {
//        Signature signature = joinPoint.getSignature();
//        Object[] params = joinPoint.getArgs();
//        String requertJson = JSON.toJSONString(params, SerializerFeature.DisableCircularReferenceDetect);
//        long startMini = System.currentTimeMillis();
//        String classAndMethod =signature.getDeclaringTypeName()+"."+signature.getName();
//        try {
//
//            Object result = joinPoint.proceed();
//            log.info("[trace-general][{}]【request={}】【response={}】[cost:{}ms]",classAndMethod,requertJson,JSON.toJSONString(result,SerializerFeature.DisableCircularReferenceDetect),System.currentTimeMillis()-startMini);
//            return result;
//        }catch (Exception e){
//            log.info("[trace-general][{}]【request={}】【response={}】[cost:{}ms]",classAndMethod,requertJson,e.getMessage(),System.currentTimeMillis()-startMini);
//            throw  e;
//        }
//
//    }
//}
