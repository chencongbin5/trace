package com.akuchen.trace.service.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TapdBug {
    private Long id;

    private String tapdBugId;

    private String workSpaceId;

    private String tid;

    private String appName;

    private Integer status;

    private Date createTime;

    private Date updateTime;

}