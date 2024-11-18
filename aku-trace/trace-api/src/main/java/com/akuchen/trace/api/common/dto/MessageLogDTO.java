package com.akuchen.trace.api.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageLogDTO implements Serializable {
    /**
     * 是否有异常堆栈
     */
    private Boolean ifExceptionStack;
}
