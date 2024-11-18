package com.akuchen.aspectj.asp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class BeanConfig {
    /**
     * 用于打印http请求日志
     * @return
     */
//    @Bean
//    public RestTemplateAspect restTemplateAspect() {
//        return new RestTemplateAspect();
//    }
//    @Bean
//    public ThrowableProxyConverterAspect throwableProxyConverterAspect() {
//        return new ThrowableProxyConverterAspect();
//    }

}
