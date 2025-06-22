package com.akuchen.trace.service.entity.dto.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class QueryReqDTO implements Serializable {

    private String tid;
    private String serviceName;
}
