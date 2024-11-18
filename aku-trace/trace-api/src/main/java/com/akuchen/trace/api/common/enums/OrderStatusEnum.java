package com.akuchen.trace.api.common.enums;

import java.util.Objects;

public enum OrderStatusEnum {
    INITIALIZATION(0, "初始化"),
    PROJECT_GENERATION(1, "生成项目"),
    MOCK_CREATION(2, "生成测试用例"),
    MOCK_TEST_CREATION(3, "生成mock类,gpt类"),
    MOCK_TEST_RUN(4, "执行debug"),
    REPLIED(5, "已回复"),
    NO_EXCEPTION_STACK(6,"没有异常堆栈信息"),
    FAILED(-1, "异常");

    private final int status;
    private final String description;

    OrderStatusEnum(int status, String description) {
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
        for (OrderStatusEnum orderStatusEnum: OrderStatusEnum.values()){
            if(Objects.equals(orderStatusEnum.getStatus(),status)){
                return orderStatusEnum.getDescription();
            }
        }
        return null;
    }
}
