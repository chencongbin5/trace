package com.akuchen.trace.service.task;

import com.akuchen.trace.api.common.dto.TapdBugDTO;
import com.akuchen.trace.api.common.enums.TapdBugStatusEnum;
import com.akuchen.trace.service.entity.dto.req.AddOrderReqDTO;
import com.akuchen.trace.service.entity.dto.rsp.TapdBugRsp;
import com.akuchen.trace.service.manager.OrderManager;
import com.akuchen.trace.service.service.TapdBugService;
import com.akuchen.trace.service.service.TraceLogService;
import com.akuchen.trace.service.util.TapdHttpClient;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *查询tapd的新创建的bug
 * @author  chencb
 *
 * */
@Slf4j
@Component
public class QueryNewBugsJob {


    @Autowired
    private TapdHttpClient tapdHttpClient;
    @Autowired
    private TapdBugService tapdBugService;
    @Autowired
    private OrderManager orderManager;

    private static final String PATTERN="yyyy-MM-dd HH:mm:ss";


    //开发环境定时任务不稳定,一会儿触发一会儿不触发的  这里换成ScheduledExecutorService
    // cron = "0 0/5 * * * ?"
     @XxlJob(value = "queryNewBugsJob")
    public void queryNewBugsJob() {
        long start = System.currentTimeMillis();
        log.info("queryNewBugsJob start......");
        // 获取当前时间 +8小时, 开发环境是0时区  tapd的数据是东八区的
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);
        String endDate = LocalDateTime.now().plusHours(8).format(formatter);
        log.info("Current time: " + endDate);

        // 获取当前时间减去6分钟的时间
        String startDate = LocalDateTime.now().minusMinutes(6).plusHours(8).format(formatter);
        log.info("Time 6 minutes ago: " + startDate);
        //查tapd6分钟内创建的bug
        List<TapdBugRsp> tapdBugRsps = tapdHttpClient.bugsWithNonNullTid(startDate, endDate);


        List<TapdBugDTO> result=new ArrayList<>();
        //创建tapd的bug
        tapdBugRsps.stream().forEach(t->{
            try {
                List<TapdBugDTO> queryDto = tapdBugService.queryByTidAndAppName(t.getLogTraceId(),t.getLogTraceAppName(),t.getBugId());
                if (CollectionUtils.isNotEmpty(queryDto)){
                    log.info("bugId:{} has been created",t.getBugId());
                    return;
                }
                //创建bug
                TapdBugDTO tapdBugDTO = new TapdBugDTO();
                tapdBugDTO.setTapdBugId(t.getBugId());
                tapdBugDTO.setWorkSpaceId(t.getWorkspaceId());
                tapdBugDTO.setTid(t.getLogTraceId());
                tapdBugDTO.setAppName(t.getLogTraceAppName());
                tapdBugDTO.setStatus(TapdBugStatusEnum.INITIALIZATION.getStatus());
                Long id = tapdBugService.insertBugSelective(tapdBugDTO);
                tapdBugDTO.setId(id);
                result.add(tapdBugDTO);
            }catch (Exception e){
                log.error("create bug error",e);
            }
        });


        //根据tapd的bug执行任务
        orderManager.tapdBugCreateOrder(result);
        log.info("queryNewBugsJob end..  row:{},....cost: {}ms", result.size(),System.currentTimeMillis() - start);

    }


    /////////////////////////////////////////


    @PostConstruct
    public void init() {
        log.info("QueryNewBugsJob init");
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(()->{
            queryNewBugsJob();
        }, 1, 3, TimeUnit.MINUTES);
    }


}


