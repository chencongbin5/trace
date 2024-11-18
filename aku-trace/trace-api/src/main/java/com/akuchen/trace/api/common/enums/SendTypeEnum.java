package com.akuchen.trace.api.common.enums;


public enum SendTypeEnum  {

    LOG(1,"log"),
    MQ(2,"mq"),

    http(3,"http"),
    ;


    private int type;
    private String desc;
    SendTypeEnum(int type, String desc){
        this.type = type;
        this.desc = desc;
    }

    public Integer getValue() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static Boolean isMq(String desc){
        return MQ.getDesc().equals(desc);
    }
}
