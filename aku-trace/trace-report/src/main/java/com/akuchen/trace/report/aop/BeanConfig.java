package com.akuchen.trace.report.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.akuchen.trace.report.TraceLogManager;
import com.akuchen.trace.report.aop.biz.BizSelector;
import com.akuchen.trace.report.log.TraceLogReportThread;
import com.akuchen.trace.report.log.common.TraceLogging;

@Configuration
@Slf4j
@ComponentScan(basePackages = {"com.akuchen.platform.components.mq.common.producer"})
public class BeanConfig {



    @Bean
    public TraceLogManager traceLogManager(){
        return new TraceLogManager();
    }

    /**
     * 使用http上报日志
     * @return
     */
    @Bean
    @ConditionalOnProperty(value="trace.report", havingValue = "http")
    public TraceLogReportThread traceLogReportThread(){
        //初始化加载日志配置
        new TraceLogging();
        return new TraceLogReportThread();
    }



    @Bean
    @ConditionalOnProperty(value="trace.enable", havingValue = "true")
    public BizSelector bizSelector(){
        return new BizSelector();
    }
}
