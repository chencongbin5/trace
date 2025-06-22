package com.akuchen.trace.parse.http;

import com.akuchen.trace.parse.utils.HttpQueryUtils;
import com.akuchen.trace.parse.utils.ValUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 测试环境模拟http请求 简化测试提供tid后的模拟发起http请求操作 常见于 测试环境 结果与预期不符的开发模拟测试之前发起的请求的操作 ,结算金额字段不对呀之类的
 *
 */
@Slf4j
public class TradebizTestHttp {

    private String tid="90926ddbade24c19b18aeb30e4ea0b63.195.16727159518049115";
    private String appName="trade-biz";
    private Integer day=0;//昨天-1  今天0
    private String ipPort="http://127.0.0.1:18022";

    //@Test
    public void mock() throws IOException {
        File logFile = new File(System.getProperty("user.dir") + "\\src\\test\\java\\com\\akuchen\\mock\\trade-tid.log");
        log.info("0 初始化");
        FileUtils.deleteQuietly(logFile);
        log.info("1 查日志");
        List<String> messages = EsQueryHttp.queryLog(day,tid,appName);
        log.info("1 查日志 行数:{}",messages.size());
        if(CollectionUtils.isEmpty(messages)){
            log.info("没有日志");
            return ;
        }
        FileUtils.writeLines(logFile,messages);
        messages = ValUtils.replace(messages);
        log.info("2 找到http请求的日志,并模拟,重新发送至指定的trade-biz:");

        String requestLog = messages.stream().filter(t ->
                t.indexOf("com.akuchen.biz.common.filter.WebLogAspect") > -1 &&
                        t.indexOf("[POST]") > -1).findFirst().orElse(null);
        if(StringUtils.isEmpty(requestLog)){
            log.info("找不到目标日志");
            return;
        }

        String url = requestLog.substring(requestLog.indexOf("[POST]") + 7, requestLog.indexOf("【header:") - 1);
        String requestJson=requestLog.substring(requestLog.indexOf("【request:")+9,requestLog.lastIndexOf("】"));
        String headerJson=requestLog.substring(requestLog.indexOf("【header:")+8,requestLog.indexOf("】"));
        JSONObject jsonObject = JSON.parseObject(headerJson);
        Set<Map.Entry<String, Object>> headers = jsonObject.entrySet();
        //日志的header和实际http请求的header名称不同 得做个映射
        Response query = HttpQueryUtils.query(ipPort + url, requestJson, headers);

        System.out.println(query);

        log.info("4 ------------------success------------------------------------");

    }






}
