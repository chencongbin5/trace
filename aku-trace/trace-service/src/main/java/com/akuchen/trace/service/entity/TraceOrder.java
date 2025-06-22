package com.akuchen.trace.service.entity;

import lombok.Data;

@Data
public class TraceOrder {

    private Long id;
    /**
     * tid
     */
    private String tid;

    private String appName;
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * git 地址
     */
    private String gitUrl;
    /**
     * 分支
     */
    private String branch;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 错误类名
     */
    private String errorClassName;
    /**
     * 错误方法名
     */
    private String errorMethodName;
    /**
     * 错误行号
     */
    private Integer errorLine;
    /**
     * 错误代码块 一般是错误行
     */
    private String errorCodeBlock;
    /**
     * 错误代码块上下文
     */
    private String errorCodeBlockContext;
    /**
     * gpt的回复
     */
    private String gptAnswer;

}
