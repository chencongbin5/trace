package com.akuchen.trace.api.common.dto;


import lombok.Data;

import java.io.Serializable;

/**
 * 统一 响应体定义
 *
 * @author hetl@akuchen.comhah  shenxp@akuchen.com
 */
@Data
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = -7160857074715919944L;

    private static final ApiResult SUCCESS = new ApiResult();

    private boolean success = true;

    private String errMsg;

    private String errCode;

    private T data;

    public ApiResult() {
    }

    public ApiResult(T t) {
        this.data = t;
    }

    public ApiResult(String errCode, String errMsg, T t) {
        this.errMsg = errMsg;
        this.errCode = errCode;
        this.data = t;
        this.success = false;
    }

    public ApiResult(boolean success, String errCode, String errMsg, T t) {
        this.success = success;
        this.errMsg = errMsg;
        this.errCode = errCode;
        this.data = t;
    }

    public static <T> ApiResult<T> errorResponse(String errCode, String errMsg, T obj) {
        return new ApiResult(errCode, errMsg, obj);
    }

    public static ApiResult errorResponse(String errCode, String errMsg) {
        return new ApiResult(errCode, errMsg, null);
    }

    public static ApiResult errorResponse(String errCode) {
        return new ApiResult(errCode, null, null);
    }

    public static <T> ApiResult<T> success(T obj) {
        return new ApiResult(obj);
    }

    public static ApiResult success() {
        return SUCCESS;
    }
}