package com.akuchen.trace.parse.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 业务类启动状态
 */
@Getter
@RequiredArgsConstructor
public enum StatusEnum {


    INIT( "初始状态",0),
    COMPELETED( "启动成功",1),

    ;

    private final String appName;
    private final Integer type;

}
