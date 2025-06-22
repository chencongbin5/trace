package com.akuchen.trace.service.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.akuchen.trace.api.common.dto.TapdBugDTO;
import com.akuchen.trace.api.common.dto.TraceOrderDTO;
import com.akuchen.trace.api.common.enums.OrderStatusEnum;
import com.akuchen.trace.api.common.enums.TapdBugStatusEnum;
import com.akuchen.trace.service.listener.event.TradeOrderUpdateEvent;
import com.akuchen.trace.service.manager.OrderManager;
import com.akuchen.trace.service.service.OrderService;
import com.akuchen.trace.service.service.TapdBugService;
import com.akuchen.trace.service.util.TapdHttpClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 任务单状态变更的后续处理
 */
@Component
@Slf4j
public class BugStatusUpdateListener {


	@Autowired
	private OrderService orderService;
	@Autowired
	private TapdBugService tapdBugService;
	private static final List<Integer> VALID_ORDER_STATUS = Arrays.asList(OrderStatusEnum.NO_EXCEPTION_STACK.getStatus(),
														   OrderStatusEnum.FAILED.getStatus(),
														   OrderStatusEnum.REPLIED.getStatus());

	@EventListener
	public void onApplicationEvent(TradeOrderUpdateEvent event) {
		//检查状态是否需要处理
		if(!VALID_ORDER_STATUS.contains(event.getStatus())){
			log.info("order status is not need to update bug status");
			return;
		}
		//如果需要
		//1查任务单的appName 和tid
		TraceOrderDTO traceOrderDTO = orderService.queryByPrimaryKey(event.getTraceOrderId());
		String answer = Objects.equals(traceOrderDTO.getStatus(), OrderStatusEnum.REPLIED.getStatus()) ?
						traceOrderDTO.getGptAnswer() :
						OrderStatusEnum.of(traceOrderDTO.getStatus());
		Integer status = Objects.equals(traceOrderDTO.getStatus(), OrderStatusEnum.REPLIED.getStatus()) ?
						 TapdBugStatusEnum.COMPLETE.getStatus() :
						 TapdBugStatusEnum.FAILED.getStatus();

		//2 根据appName 和 tid 查出所有的bug单(状态非终态)
		List<TapdBugDTO> tapdBugDTOS =
				tapdBugService.queryByTidAndAppName(traceOrderDTO.getTid(), traceOrderDTO.getAppName(),null);
		//过滤状态
		List<Integer> statusList =
				Arrays.asList(TapdBugStatusEnum.COMPLETE.getStatus(), TapdBugStatusEnum.FAILED.getStatus());
		List<TapdBugDTO> filterTapdBugDTOs =
				tapdBugDTOS.stream().filter(t -> !statusList.contains(t.getStatus())).collect(Collectors.toList());

		filterTapdBugDTOs.stream().forEach(tapdBugDTO -> {
			tapdBugService.commitBug(tapdBugDTO, answer,status);
		});


	}
}
