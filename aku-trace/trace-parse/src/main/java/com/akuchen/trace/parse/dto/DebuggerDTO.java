package com.akuchen.trace.parse.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DebuggerDTO {
    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 方法行数
     */
    private Integer methodLine;

    /**
     * 可能存在方法名相同的情况,所以需要用index来区分
     */
    private Integer methodIndex;


    /**
     * 这一行的代码
     */
    private String codeSource;

    /**
     * 类行数
     */
    private Integer classLine;
    /**
     * 异常堆栈
     */
    private String stack;



}
