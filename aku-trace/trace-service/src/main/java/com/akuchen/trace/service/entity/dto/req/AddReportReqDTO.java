package com.akuchen.trace.service.entity.dto.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddReportReqDTO implements Serializable {


	private String tid;

	private String serviceName;

	private String log;

	/**
	 * 这里是大批量 不同于mq  所以没有 threadName 和LogTime
	 */

}
