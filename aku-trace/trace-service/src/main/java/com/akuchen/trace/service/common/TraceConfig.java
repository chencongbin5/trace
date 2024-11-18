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
    //private String authorization="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJkZXB0OmVkaXQsdXNlcjpsaXN0LHN0b3JhZ2U6YWRkLGRlcHQ6YWRkLHN0b3JhZ2U6ZWRpdCxtZW51OmRlbCxyb2xlczpkZWwsYWRtaW4sc3RvcmFnZTpsaXN0LGpvYjplZGl0LGRlcGxveUhpc3Rvcnk6bGlzdCx1c2VyOmRlbCxzZXJ2ZXI6bGlzdCxkaWN0OmFkZCxkZXB0Omxpc3QsdGltaW5nOmFkZCxqb2I6bGlzdCxkaWN0OmRlbCxkaWN0Omxpc3QsYXBwOmxpc3Qsam9iOmFkZCxkYXRhYmFzZTpsaXN0LHRpbWluZzpsaXN0LGRlcGxveTpsaXN0LHJvbGVzOmFkZCx1c2VyOmFkZCxwaWN0dXJlczpsaXN0LG1lbnU6ZWRpdCx0aW1pbmc6ZWRpdCxtZW51Omxpc3Qsc3RvcmFnZTpkZWwscm9sZXM6bGlzdCxtZW51OmFkZCxqb2I6ZGVsLHVzZXI6ZWRpdCxyb2xlczplZGl0LHRpbWluZzpkZWwsZGljdDplZGl0LHNlcnZlckRlcGxveTpsaXN0LGRlcHQ6ZGVsIiwiZXhwIjoxNzE0OTk4NzMyfQ.0WKpboiETy3QDzF0W8LQqIMtQCY13wpLHdkZYPomwSck1FxaF8BTkKyuR-4TXqSzLswFXjLRWVzxQNzEA4bbYQ";
    private String authorization="Iapi 4rFHq5OPCLncvmBu+3VnURfi9Q/9A2rANOhAH4OB7vg=";

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
