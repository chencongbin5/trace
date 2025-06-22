package com.akuchen.trace.service.service;

import com.akuchen.trace.api.common.dto.TraceOrderDTO;
import com.akuchen.trace.api.common.utils.BeanUtils;
import com.akuchen.trace.service.entity.TraceOrder;
import com.akuchen.trace.service.listener.event.TradeOrderUpdateEvent;
import com.akuchen.trace.service.mapper.TraceOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private TraceOrderMapper traceOrderMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;



    public Long insert(TraceOrderDTO traceOrderDto) {
        TraceOrder traceOrder = BeanUtils.convert(traceOrderDto,TraceOrder.class);
        int i = traceOrderMapper.insertSelective(traceOrder);
        return traceOrder.getId();
    }

    public TraceOrderDTO queryByPrimaryKey(Long id){
        TraceOrder traceOrder = traceOrderMapper.selectByPrimaryKey(id);
        return BeanUtils.convert(traceOrder, TraceOrderDTO.class);
    }
    public TraceOrderDTO queryLastByTid(String tid){
        TraceOrder traceOrder = traceOrderMapper.selectByTid(tid);
        return BeanUtils.convert(traceOrder, TraceOrderDTO.class);
    }
    public TraceOrderDTO queryByTidAndAppName(String tid,String appName){
        TraceOrder traceOrder = traceOrderMapper.selectByTidAndAppName(tid,appName);
        return BeanUtils.convert(traceOrder, TraceOrderDTO.class);
    }

    public Integer modifyStatus(Long orderId, Integer status) {
        TraceOrderDTO traceOrderDto=new TraceOrderDTO();
        traceOrderDto.setId(orderId);
        traceOrderDto.setStatus(status);
        updateByPrimaryKeySelective(traceOrderDto);
        return 0;
    }

    public Integer updateByPrimaryKeySelective(TraceOrderDTO traceOrderDto){
        TraceOrder traceOrder = BeanUtils.convert(traceOrderDto,TraceOrder.class);
        int row = traceOrderMapper.updateByPrimaryKeySelective(traceOrder);
        if(row>0){
            //通知bug单更新状态
            TradeOrderUpdateEvent event =
                    new TradeOrderUpdateEvent(this, traceOrder.getId(), traceOrder.getStatus());
            eventPublisher.publishEvent(event);
        }
        return row;
    }


}
