package com.akuchen.trace.service.entity.dto.rsp;

import lombok.Data;

import java.io.Serializable;
@Data
public class TapdBugRsp implements Serializable {

    private String bugId;
    private String workspaceId;
    private String title;
    private String description;
    private String priority;
    private String severity;
    private String status;
    private String module;
    private String owner;
    private String cc;
    private String reporter;
    private String tester;
    private String developer;
    private String closer;
    private String modifier;
    private Long createTime;
    private Long closedTime;
    private Long modifiedTime;
    /**
     * 自定义字段tid
     */
    private String logTraceId;
    /**
     * 自定义字段appName
     */
    private String logTraceAppName;
}
