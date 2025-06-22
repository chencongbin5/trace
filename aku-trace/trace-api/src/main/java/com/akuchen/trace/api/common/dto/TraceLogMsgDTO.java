package com.akuchen.trace.api.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TraceLogMsgDTO implements Serializable {

    private String tid;

    private String serviceName;

    private String threadName;

    private Date logTime;

    private String log;

}
