package com.akuchen.trace.service.common;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.Map;

@Slf4j
@Setter
@Getter
@ConfigurationProperties(prefix = "trace-service")
@Configuration
public class TraceConfig {

    private  String gitlabUsername;

    private  String gitlabPassword;

    private String servicePath;

    /**
     * 命令的超时时间 单位毫秒 暂定10分钟吧 搞个巨大值
     */
    private Long commandTimeout=600000L;

    private Map<String,application> applications;

    /**
     * 如花的访问地址
     */
    //private String tapdPreFix="http://localhost:6006";
    private String tapdPreFix="https://cicd.line8.al.com";


    /**
     * 如花的workspaceId  tapd的项目id
     */
    private String tapdWorkspaceId="34425100";
    /**
     * 如花的token
     */
    private String authorization="xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    @Data
    public static class application implements Serializable {

        /**
         * 实际git上的最小目录下的项目名称
         * 如果为空  说明没有子目录 这就是最小目录
         */
        private String serviceName;
        /**
         * gitlab地址
         */
        private String gitlabUrl;
        /**
         * 启动类
         */
        private String mainClass;

        private String elk;
    }
}
