package com.akuchen.trace.service.entity.dto.rsp;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseRsp<T> implements Serializable {
    private Integer code;

    private String msg;

    private T data;
}
