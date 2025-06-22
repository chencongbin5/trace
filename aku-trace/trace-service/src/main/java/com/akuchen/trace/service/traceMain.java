package com.akuchen.trace.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication
//@EnableDubbo
@MapperScan("com.akuchen.trace.service.mapper")
@EnableCaching
public class traceMain {
    public static void main(String[] args) {
        SpringApplication.run(traceMain.class,args);
    }
}
