package com.akuchen.trace.api.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TapdBugDTO implements Serializable {
    private Long id;

    private String tapdBugId;

    private String workSpaceId;

    private String tid;

    private String appName;

    private Integer status;

    private Date createTime;

    private Date updateTime;
}
