package com.akuchen.trace.api.common.enums;

import java.util.Objects;

public enum TapdBugStatusEnum {
    INITIALIZATION(0, "初始化"),
    CREATE_ORDER(1, "创建任务"),
    COMPLETE(2, "完成"),
    FAILED(-1, "异常"),
    CREATE_ORDER_FAILED(-2, "创建任务失败"),//创建失败.需要重试,
    ;

    private final int status;
    private final String description;

    TapdBugStatusEnum(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public static String of(Integer status){
        for (TapdBugStatusEnum orderStatusEnum: TapdBugStatusEnum.values()){
            if(Objects.equals(orderStatusEnum.getStatus(),status)){
                return orderStatusEnum.getDescription();
            }
        }
        return null;
    }
}
