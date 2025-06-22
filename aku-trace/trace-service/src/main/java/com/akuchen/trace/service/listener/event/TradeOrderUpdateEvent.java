package com.akuchen.trace.service.listener.event;

import org.springframework.context.ApplicationEvent;

/**
 * 任务单状态变更事件
 */
public class TradeOrderUpdateEvent extends ApplicationEvent {

	private Long traceOrderId;
	private Integer status;

	public TradeOrderUpdateEvent(Object source,Long traceOrderId,Integer status) {
		super(source);
		this.traceOrderId=traceOrderId;
		this.status=status;
	}


	public Integer getStatus() {
		return status;
	}

	public Long getTraceOrderId() {
		return traceOrderId;
	}
}
