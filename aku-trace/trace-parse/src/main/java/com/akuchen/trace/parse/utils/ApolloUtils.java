package com.akuchen.trace.parse.utils;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * apollo 主要是加密gpt token
 */
public class ApolloUtils {

    private static String url;
    private static String code;

    private static List<String> wrhiteListFields;

    public static String getUrl() {
        if (StringUtils.isEmpty(url)) {
            init();
        }
        return url;
    }
    public static String getCode() {
        if (StringUtils.isEmpty(code)) {
            init();
        }
        return code;
    }
    public static List<String> getWrhiteListFields() {
        if (CollectionUtils.isEmpty(wrhiteListFields)) {
            init();
        }
        return wrhiteListFields;
    }

    public static void init() {
        // 设置Apollo配置
        System.setProperty("app.id", "trace-service");
        System.setProperty("apollo.meta", "http://192.168.3.247:9090");

        System.setProperty("env", "test");
        // 获取Apollo配置
        Config config = ConfigService.getConfig("share01.trace");
        url = config.getProperty("trace.gptUrl", null);
        code = config.getProperty("trace.gptCode", null);
        String wrhiteListFieldsVal = config.getProperty("wrhiteListFields", null);
        Optional.ofNullable(wrhiteListFieldsVal).ifPresent(v->{
            wrhiteListFields = Arrays.asList(v.split(","));
        });
    }

}
