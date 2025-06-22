package com.akuchen.trace.service.util;

import com.akuchen.trace.api.common.dto.TraceOrderDTO;
import com.akuchen.trace.api.common.dto.TraceLogMsgDTO;
import com.akuchen.trace.api.common.enums.OrderStatusEnum;
import com.akuchen.trace.service.entity.TraceLog;
import com.akuchen.trace.service.entity.dto.req.AddOrderReqDTO;

/**
 * 转换类
 */
public class ConvertUtils {

    public static TraceLog toTraceLog(TraceLogMsgDTO traceLogMsgDTO){
        TraceLog traceLog = new TraceLog();
        traceLog.setTid(traceLogMsgDTO.getTid());
        traceLog.setThreadName(traceLogMsgDTO.getThreadName());
        traceLog.setLogTime(traceLogMsgDTO.getLogTime());
        traceLog.setLog(traceLogMsgDTO.getLog());
        traceLog.setServiceName(traceLogMsgDTO.getServiceName());

        return traceLog;
    }

    public static TraceOrderDTO toOrderDto(AddOrderReqDTO addOrderReqDTO, String gitlabUrl, String codePath, String serviceName){
        TraceOrderDTO traceOrderDto = new TraceOrderDTO();
        traceOrderDto.setTid(addOrderReqDTO.getTid());
        traceOrderDto.setAppName(addOrderReqDTO.getAppName());
        traceOrderDto.setServiceName(serviceName);
        traceOrderDto.setDay(addOrderReqDTO.getDay());
        traceOrderDto.setGitUrl(gitlabUrl);
        traceOrderDto.setCodePath(codePath);
        traceOrderDto.setBranch("test");
        traceOrderDto.setStatus(OrderStatusEnum.PROJECT_GENERATION.getStatus());
        return traceOrderDto;
    }
}
