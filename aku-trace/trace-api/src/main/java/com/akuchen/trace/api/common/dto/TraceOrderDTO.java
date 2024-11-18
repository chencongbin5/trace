package com.akuchen.trace.api.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TraceOrderDTO implements Serializable {
    private Long id;
    private String tid;

    /**
     * 项目名称, 白象里面这个app叫什么, 这里就叫什么,
     */
    private String appName;
    private String gitUrl;
    private String codePath;
    private String branch;
    /**
     * 今天0 , 昨天-1 前天-2 搜索用
     */
    private Integer day;

    private Integer status;
    /**
     * 展示用
     */
    private String statusName;
    /**
     * 服务名称
     */
    private String serviceName;
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
