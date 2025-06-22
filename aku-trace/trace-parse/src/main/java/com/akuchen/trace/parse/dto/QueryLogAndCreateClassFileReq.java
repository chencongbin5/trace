package com.akuchen.trace.parse.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class QueryLogAndCreateClassFileReq implements Serializable {
    /**
     * 日志tid
     */
    private String tid;
    /**
     * 项目名称
     * @see
     */
    private String appName;
    /**
     *日期  日志的索引和日期有关系,所以需要确定是哪天的日志
     * 0今天
     * -1昨天
     * -2前天
     */
    private Integer day;
    /**
     * .java 文件的生成路径  直接放test目录下,
     * eg: System.getProperty("user.dir") + "\\src\\test\\java\\com\\akuchen\\ccb"
     */
    private String outFilePath;
    /**
     * 项目main入口
     * eg: com.akuchen.service.calculation.CalculationServiceApplication
     */
    private String mainClass;
    /**
     * 参数是否设置为any()
     * 基于随机规则或者时间戳规则生成的字段通常无法和日志的参数字段相匹配，
     * 建议一开始设置为false，mock文件启动未按照预期进行的时候改成true
     */
    private boolean whenParamsIsAny;
    /**
     * 是否自动运行
     * true: 生成mock文件后自动运行  mock文件可能会生成多个@test用例, 会自动运行所有的用例,部分用例可能你并不关心
     * false: 生成mock文件后不自动运行
     */
    private boolean autorun;
    /**
     * 是否检查bug
     */
    private Boolean chatGPTBug;
    /**
     * 自动运行gpt
     */
    private Boolean chatGPTBugAutorun;
    /**
     *默认log
     * @see com.akuchen.trace.api.common.enums.SendTypeEnum
     */
    private String report= "log";

    /**
     * 正式环境用到
     * 测试环境不用
     */
    private String cookie;

    /**
     * elk配置  默认aku
     */
    private String elk="aku";
}
