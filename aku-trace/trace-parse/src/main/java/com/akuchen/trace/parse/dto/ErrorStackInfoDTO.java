package com.akuchen.trace.parse.dto;

import lombok.Data;

import java.util.List;

@Data
public class ErrorStackInfoDTO {
    /**
     * 错误信息
     */
    private  String errorMessage;

    /**
     * 错误堆栈
     */
    private List<String> stackLines;

}
