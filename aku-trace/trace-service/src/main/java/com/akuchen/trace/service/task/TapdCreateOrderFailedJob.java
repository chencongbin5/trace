package com.akuchen.trace.service.task;

import com.akuchen.trace.api.common.dto.TapdBugDTO;
import com.akuchen.trace.service.manager.OrderManager;
import com.akuchen.trace.service.service.TapdBugService;
import com.xxl.job.core.biz.model.ReturnT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *tapd创建任务失败的job
 * 重新尝试创建任务
 * 创建任务失败原因
 * 1.当前项目正在执行任务
 * 2.当前项目没有配置apollo
 * @author  chencb
 *
 * */
@Slf4j
@Component
public class TapdCreateOrderFailedJob {

    @Autowired
    private TapdBugService tapdBugService;
    @Autowired
    private OrderManager orderManager;


    //开发环境定时任务不稳定,一会儿触发一会儿不触发的  这里换成ScheduledExecutorService 也可以随时切换成xxl-job
    // cron = "0 0/3 * * * ?"
    //@XxlJob(value = "tapdCreateOrderFailedJob")
    public ReturnT<String> tapdCreateOrderFailedJob() {
        log.info("tapdCreateOrderFailedJob start......");
        // 获取一天之内创建任务失败的bug单 重试
        List<TapdBugDTO> tapdBugDTOS = tapdBugService.queryCreateOrderFailedBugs();
        //根据tapd的bug执行任务
        orderManager.tapdBugCreateOrder(tapdBugDTOS);
        return ReturnT.SUCCESS;
    }



    @PostConstruct
    public void init() {
        log.info("tapdCreateOrderFailedJob init");
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(()->{
            tapdCreateOrderFailedJob();
        }, 1, 3, TimeUnit.MINUTES);
    }
}


