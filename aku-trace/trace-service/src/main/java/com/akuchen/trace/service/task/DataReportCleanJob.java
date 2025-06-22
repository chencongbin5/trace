package com.akuchen.trace.service.task;


import com.akuchen.trace.service.service.TraceLogService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 此类需要重构,通过配置的方式,用xxl-job实现 或者用 Spring @Scheduled 注解
 * @author  chencb
 *
 * */
@Slf4j
@Component
public class DataReportCleanJob {

    @Autowired
    private TraceLogService traceLogService;

    // cron = "0 0/10 * * * ?
    @XxlJob(value = "dataReportCleanJob")
    public ReturnT<String> dataReportCleanJob() {
        long start = System.currentTimeMillis();
        log.info("dataReportCleanJob start......");
        Integer row = traceLogService.deleteDataByCreateTime();
        log.info("dataReportCleanJob end.. row:{}....cost: {}ms", row, System.currentTimeMillis() - start);
        return ReturnT.SUCCESS;
    }
}


