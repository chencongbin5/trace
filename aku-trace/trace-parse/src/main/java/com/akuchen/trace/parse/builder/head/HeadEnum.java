package com.akuchen.trace.parse.builder.head;

import lombok.Data;
import lombok.Getter;

@Getter
public enum HeadEnum {

    RPC("[trace-rpc-provider]", new RpcHeadImpl()),
    MQ("[trace-mq]", new MqHeadImpl()),
    JOB("[trace-job]", new JobHeadImpl()),
    BIZ("[trace-biz]", new BizHeadImpl());

    private String type;
    private IHead head;

    HeadEnum(String type, IHead head) {
        this.type = type;
        this.head = head;
    }

    public static IHead getHead(String type) {
        for (HeadEnum headEnum : HeadEnum.values()) {
            if (headEnum.type.equals(type)) {
                return headEnum.head;
            }
        }
        return null;
    }
}
