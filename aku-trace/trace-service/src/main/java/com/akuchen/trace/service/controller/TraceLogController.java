package com.akuchen.trace.service.controller;

import com.akuchen.trace.api.common.dto.ApiResult;
import com.akuchen.trace.api.common.dto.TraceLogMsgDTO;
import com.akuchen.trace.api.common.dto.TraceOrderDTO;
import com.akuchen.trace.api.common.enums.OrderStatusEnum;
import com.akuchen.trace.service.entity.dto.req.AddOrderReqDTO;
import com.akuchen.trace.service.entity.dto.req.QueryReqDTO;
import com.akuchen.trace.service.entity.dto.req.QueryTraceOrderReq;
import com.akuchen.trace.service.manager.OrderManager;
import com.akuchen.trace.service.service.OrderService;
import com.akuchen.trace.service.service.TraceLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping({"api/traceLog"})
public class TraceLogController {

    @Autowired
    private TraceLogService traceLogService;

    @Autowired
    private OrderManager orderManager;

    @Autowired
    private OrderService orderService;

    /**
     * 根据tid查询trace日志
     */
    @PostMapping(value = "/query", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ApiResult<List<TraceLogMsgDTO>> query(@RequestBody QueryReqDTO queryReqDTO) {
        return ApiResult.success(traceLogService.query(queryReqDTO.getTid(),queryReqDTO.getServiceName()));
    }

    /**
     * 创建新任务
     */
    @PostMapping(value = "/addOrder", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ApiResult<Boolean> addOrder(@RequestBody AddOrderReqDTO addOrderReqDTO) {
        Boolean flag = orderManager.createOrder(addOrderReqDTO);
        if(flag){
            return ApiResult.success(flag);
        }
        return  ApiResult.errorResponse("101","项目正在执行中");

    }

    /**
     * 查tid的执行情况
     * @param queryTraceOrderReq
     * @return
     */
    @PostMapping(value = "/queryTraceOrder", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ApiResult<TraceOrderDTO> queryTraceOrder(@RequestBody QueryTraceOrderReq queryTraceOrderReq){
        TraceOrderDTO traceOrderDto = orderService.queryLastByTid(queryTraceOrderReq.getTid());
        Optional.ofNullable(traceOrderDto).ifPresent(t->t.setStatusName(OrderStatusEnum.of(t.getStatus())));
        return ApiResult.success(traceOrderDto);

    }


}
