package com.akuchen.trace.service.entity.dto.req;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class QueryTraceOrderReq implements Serializable {

    @NotNull
    private String tid;
}
