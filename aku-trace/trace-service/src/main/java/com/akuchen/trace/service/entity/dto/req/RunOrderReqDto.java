package com.akuchen.trace.service.entity.dto.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class RunOrderReqDto implements Serializable {
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
}
