package com.akuchen.trace.service.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TraceLog {
    private Long id;

    private String tid;

    private String serviceName;

    private String threadName;

    private Date logTime;

    private Date createTime;

    private Date updateTime;

    private String log;


}