package com.akuchen.trace.service.entity.dto.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddOrderReqDTO implements Serializable {

    private String tid;

    /**
     * 项目名称, 白象里面这个app叫什么, 这里就叫什么,
     */
    private String appName;
    private Integer day;
    /**
     * db的bug单id
     */
    private String tapdBugId;
    private Long bugId;
}
